package com.legendsofvaleros.modules.quests;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.PromiseCache;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.quests.api.*;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;
import com.legendsofvaleros.modules.quests.core.*;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.registry.EventRegistry;
import com.legendsofvaleros.modules.quests.registry.NodeRegistry;
import com.legendsofvaleros.modules.quests.registry.PrerequisiteRegistry;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class QuestAPI extends ListenerModule {
    public interface RPC {
        Promise<IQuest> getQuest(String questId);

        /**
         * Quest progress is stored in a way that cannot be decoded by Gson. We, unfortunately, must do it manually,
         * as children of elements highly depend on the values of the parent.
         * <p/>
         * {
         * questID: {
         * history: [
         * { date: "yyyy-MM-dd HH:mm:ss", event: "ENDED" },
         * { date: "yyyy-MM-dd HH:mm:ss", event: "STARTED" },
         * { date: "yyyy-MM-dd HH:mm:ss", event: "FAILED" },
         * { date: "yyyy-MM-dd HH:mm:ss", event: "STARTED" }
         * ],
         * nodes: {
         * UUID: { data },
         * UUID: { data }
         * }
         * }
         * }
         */
        Promise<Map<String, JsonObject>> getPlayerQuestsProgress(CharacterId characterId);

        Promise<Boolean> savePlayerQuestsProgress(CharacterId characterId, Map<String, Object> map);

        Promise<Boolean> deletePlayerQuestsProgress(CharacterId characterId);
    }

    private EventRegistry eventRegistry;
    private NodeRegistry nodeRegistry;
    private PrerequisiteRegistry prerequisiteRegistry;

    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }

    public NodeRegistry getNodeRegistry() {
        return nodeRegistry;
    }

    public PrerequisiteRegistry getPrerequisiteRegistry() {
        return prerequisiteRegistry;
    }

    private RPC rpc;

    //private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Multimap<CharacterId, IQuestInstance> playerQuestsLoaded = HashMultimap.create();

    /**
     * A map containing each user to a list of all quests instances they own. This also has the effect of keeping
     * the quests in memory. Once they're ejected from this, providing they aren't referenced in any NPC, they'll be
     * invalidated in the caches below.
     */
    private Multimap<CharacterId, IQuestInstance> playerQuests = HashMultimap.create();

    private PromiseCache<String, IQuest> quests;

    @Override
    public void onLoad() {
        super.onLoad();

        this.eventRegistry = new EventRegistry();
        this.nodeRegistry = new NodeRegistry();
        this.prerequisiteRegistry = new PrerequisiteRegistry();

        this.rpc = APIController.create(RPC.class);

        this.quests = new PromiseCache<>(CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakValues()
                .removalListener(entry -> {
                    getLogger().warning("Quest '" + entry.getKey() + "' removed from the cache: " + entry.getCause());
                })
                .build(), questId -> rpc.getQuest(questId));

        getScheduler().executeInMyCircleTimer(() -> {
            // This is done so we get almost-live updates on GC'd listeners.
            quests.cleanUp();
        }, 0L, 20L);

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(IQuest.class, (JsonDeserializer<IQuest>) (json, typeOfT, context) -> {
                    // If it's an object, then we're decoding the actual quest.
                    if(json.isJsonObject()) {
                        try {
                            return decodeQuest(json.getAsJsonObject(), context);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        // If it's not an object, then it's a reference to the quest.
                        // Note: the quest must already be loaded, or the value will be null.
                        return quests.getIfPresent(json.getAsString());
                    }
                })
                .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, typeOfT, context) -> Duration.parse(json.getAsString()));

        registerEvents(new PlayerListener());
    }

    public Collection<IQuestInstance> getPlayerQuests(PlayerCharacter pc) {
        return playerQuests.get(pc.getUniqueCharacterId());
    }

    public Promise<IQuest> getQuest(String quest_id) {
        return quests.get(quest_id);
    }

    public void addPlayerQuest(PlayerCharacter pc, IQuestInstance instance) {
        playerQuests.put(pc.getUniqueCharacterId(), instance);
    }

    private Promise<Void> onLogin(final PlayerCharacter pc) {
        Promise<Void> promise = new Promise<>();

        rpc.getPlayerQuestsProgress(pc.getUniqueCharacterId())
                .onFailure(promise::reject).onSuccess(val -> {
            Map<String, JsonObject> map = val.orElse(ImmutableMap.of());

            if (map.size() == 0) {
                promise.resolve(null);
                return;
            }

            AtomicInteger questsToLoad = new AtomicInteger(map.size());

            map.forEach((k, v) -> {
                getQuest(k).onSuccess(q -> {
                    if (q.isPresent()) {
                        try {
                            IQuestInstance instance = APIController.getInstance().getGson().fromJson(v, QuestInstance.class);

                            // Store the instance in a temporary map for safety, as we don't act like it's ready until
                            // the player is completely loaded.
                            playerQuestsLoaded.put(pc.getUniqueCharacterId(), instance);
                        } catch (Exception e) {
                            getLogger().warning("Player attempt to load progress for quest, but something went wrong. Offender: " + pc.getPlayer().getName() + " in quest " + k);
                            MessageUtil.sendSevereException(this, pc.getPlayer(), e);
                        }
                    }
                }).on(() -> {
                    if (questsToLoad.decrementAndGet() == 0)
                        promise.resolve(null);
                });
            });
        });

        return promise;
    }

    private void onLoginComplete(PlayerCharacter pc) {
        // Set the instance in the quest after loading has completed. This is because the quest may act on the
        // addition immediately, and we don't want anything occurring during player load.
        for (IQuestInstance instance : playerQuestsLoaded.get(pc.getUniqueCharacterId())) {
            playerQuests.put(pc.getUniqueCharacterId(), instance);
            instance.getQuest().setInstance(pc.getUniqueCharacterId(), instance);
        }

        playerQuestsLoaded.removeAll(pc.getUniqueCharacterId());
    }

    /**
     * Save player quest progress to database.
     */
    private Promise<Boolean> onLogout(PlayerCharacter pc) {
        // Remove the instance from quests.
        for (IQuestInstance instance : getPlayerQuests(pc))
            instance.getQuest().removeInstance(pc.getUniqueCharacterId());

        Map<String, Object> map = new HashMap<>();

        for (IQuestInstance instance : playerQuests.removeAll(pc.getUniqueCharacterId()))
            map.put(instance.getQuest().getId(), instance);

        return this.rpc.savePlayerQuestsProgress(pc.getUniqueCharacterId(), map);
    }

    private Promise<Boolean> onDelete(final PlayerCharacter pc) {
        for (IQuestInstance instance : getPlayerQuests(pc))
            instance.getQuest().removeInstance(pc.getUniqueCharacterId());

        playerQuests.removeAll(pc.getUniqueCharacterId());

        return this.rpc.deletePlayerQuestsProgress(pc.getUniqueCharacterId());
    }

    public void reloadQuests() throws Throwable {
        // Make all quests think the user has logged out.
        for (CharacterId characterId : playerQuests.keys()) {
            onLogout(Characters.getPlayerCharacter(characterId)).get();
        }

        playerQuests.clear();

        quests.invalidateAll();
        quests.cleanUp();

        // This shouldn't ever fire, but it's here just in case.
        if (quests.size() > 0)
            MessageUtil.sendException(this, quests.size() + " quests did not get cleared from the cache.");

        // Make all quests think the user has logged in.
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!Characters.isPlayerCharacterLoaded(p)) continue;
            onLogin(Characters.getPlayerCharacter(p)).get();
        }
    }

    /**
     * Propagate caught events to all quest instances.
     */
    public void propagateEvent(PlayerCharacter pc, Class<? extends Event> caught, Event event) {
        for (IQuestInstance instance : playerQuests.get(pc.getUniqueCharacterId()))
            instance.getQuest().callEvent(instance, caught, event);
    }

    /**
     * Theoretically we don't need to <i>enocode</i> the resulting object for it to be savable, only decode.
     *
     * @param jo
     * @param context
     */
    private IQuest decodeQuest(JsonObject jo, JsonDeserializationContext context) throws NoSuchFieldException, IllegalAccessException {
        IQuestPrerequisite[] prerequisites = new IQuestPrerequisite[0];
        {
            // TODO: Decode prerequisites
        }

        Object repeat = jo.get("repeat");
        {
            // TODO: Decode repeat parameters
        }

        QuestNodeMap nodes = new QuestNodeMap();
        if (jo.has("nodes")) {
            for (Map.Entry<String, JsonElement> nodeEntry : jo.getAsJsonObject("nodes").entrySet()) {
                UUID id = UUID.fromString(nodeEntry.getKey());
                JsonObject obj = nodeEntry.getValue().getAsJsonObject();

                Optional<Class<? extends IQuestNode>> clazz = this.getNodeRegistry().getType(
                        obj.get("type").getAsString()
                );

                if (!clazz.isPresent()) {
                    throw new IllegalArgumentException("Unknown node type '" + obj.get("type").getAsString() + "' in quest '" + jo.get("_id").getAsString() + "'!");
                }


                Class<? extends IQuestNode> nodeClass = clazz.get();
                IQuestNode node = null;

                try {
                    node = nodeClass.getDeclaredConstructor(UUID.class).newInstance();
                } catch (Exception e) {
                    // Silence intellij. There will never be a time that this constructor is not defined.
                }

                // Decode default input interface values
                for(Map.Entry<String, JsonElement> optionEntry : obj.getAsJsonObject("interfaces").entrySet()) {
                    IInportValue in = (IInportValue)findField(IInportValue.class, nodeClass, optionEntry.getKey()).get(node);
                    in.setDefaultValue(APIController.getInstance().getGson().fromJson(optionEntry.getValue(), in.getValueClass()));
                }

                // Decode options
                for(Map.Entry<String, JsonElement> optionEntry : obj.getAsJsonObject("options").entrySet()) {
                    Field f = findField(Object.class, nodeClass, optionEntry.getKey());
                    f.set(node, APIController.getInstance().getGson().fromJson(optionEntry.getValue(), f.getType()));
                }

                nodes.put(id, node);
            }
        }

        // Decode connections
        if(jo.has("connections")) {
            for(JsonElement con : jo.getAsJsonArray("connections")) {
                JsonObject pair = con.getAsJsonObject();

                JsonArray from = pair.getAsJsonArray("from");
                JsonArray to = pair.getAsJsonArray("to");

                IQuestNode fromNode = nodes.get(UUID.fromString(from.get(0).getAsString()));
                IQuestNode toNode = nodes.get(UUID.fromString(to.get(0).getAsString()));

                INodeOutput out = (INodeOutput)findField(INodeOutput.class, fromNode.getClass(), from.get(1).getAsString()).get(fromNode);
                INodeInput in = (INodeInput)findField(INodeInput.class, toNode.getClass(), to.get(1).getAsString()).get(fromNode);

                out.addConnection(in);
                in.setConnection(out);
            }
        }

        return new Quest(jo.get("_id").getAsString(),
                jo.get("slug").getAsString(),
                jo.get("name").getAsString(),
                jo.get("description").getAsString(),
                jo.get("forced").getAsBoolean(),
                prerequisites, repeat, nodes);
    }

    private Field findField(Class<?> type, Class<?> clazz, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = null;
        try {
            field = clazz.getField(name);

            if(type.isAssignableFrom(field.getType())) {
                field = null;
            }
        } catch (NoSuchFieldException e) {

        }

        if(field == null) {
            for(Field f : clazz.getFields()) {
                if(!type.isAssignableFrom(f.getType())) continue;

                SerializedName serializedName = f.getAnnotation(SerializedName.class);
                if(serializedName != null) {
                    if(serializedName.value().equals(name)) {
                        field = f;
                        break;
                    }
                }
            }
        }

        if(field == null)
            throw new NoSuchFieldException(name);
        
        return field;
    }

    /**
     * Theoretically we don't need to <i>encode</i> the resulting object for it to be savable, only decode.
     */
    /*private IQuestInstance decodeQuestInstance(PlayerCharacter pc, IQuest quest, JsonObject jo) {
        QuestInstance instance = new QuestInstance(pc, quest);

        // If quest log data exists for this quest, decode it.
        if (jo.has("log")) {
            for(Map.Entry<Integer, QuestLogEntry> entry : APIController.getInstance().getGson().fromJson(jo.get("log"), QuestLogMap.class).entrySet())
                instance.setLogEntry(entry.getKey(), entry.getValue());
        }

        // If historical data exists for this quest, decode it.
        if (jo.has("history")) {
            instance.addHistory(APIController.getInstance().getGson().fromJson(jo.get("history"), IQuestHistory[].class));
        }

        // If this quest has node instances saved, decode them.
        if (jo.has("nodes")) {
            JsonObject obj = jo.getAsJsonObject("nodes");

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                // Get the node information from the quest using the UUID key
                Optional<IQuestNode> ret = quest.getNode(UUID.fromString(entry.getKey()));

                // Node gone. Ignore it.
                if (!ret.isPresent())
                    continue;

                IQuestNode node = ret.get();

                // Decode the node instance using the value
                Object nodeInstance = APIController.getInstance().getGson().fromJson(
                        entry.getValue(),
                        this.getNodeRegistry().getInstanceType(node.getClass())
                );

                instance.setNodeInstance(node, nodeInstance);
            }
        }

        return instance;
    }*/

    private class PlayerListener implements Listener {
        @EventHandler
        public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Quests");

            onLogin(event.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler
        public void onCharacterFinishLoading(PlayerCharacterFinishLoadingEvent event) {
            onLoginComplete(event.getPlayerCharacter());
        }

        @EventHandler
        public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Quests");

            onLogout(event.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler
        public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
            onDelete(event.getPlayerCharacter());
        }
    }
}
