package com.legendsofvaleros.modules.quests;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
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
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;
import com.legendsofvaleros.modules.quests.core.Quest;
import com.legendsofvaleros.modules.quests.core.QuestInstance;
import com.legendsofvaleros.modules.quests.core.QuestNodeMap;
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
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuestAPI extends ListenerModule {
    public interface RPC {
        Promise<Quest> getQuest(Object query);

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
        Promise<List<QuestInstance>> getPlayerQuests(CharacterId characterId);

        Promise<Object> savePlayerQuests(CharacterId characterId, Collection<IQuestInstance> quests);

        Promise<Boolean> deletePlayerQuests(CharacterId characterId);
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

    private PromiseCache<String, Quest> quests;

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
                .build(), id -> rpc.getQuest(id));

        InterfaceTypeAdapter.register(IQuest.class,
                                        obj -> obj.getId(),
                                        id -> quests.getAndWait(id).orElse(null));

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(Quest.class, (JsonDeserializer<Quest>) (json, typeOfT, context) -> {
                    try {
                        return decodeQuest(json.getAsJsonObject(), context);
                    } catch (IllegalAccessException e) {
                        MessageUtil.sendException(this, e);
                    }

                    return null;
                });

        registerEvents(new PlayerListener());
    }

    public Collection<IQuestInstance> getPlayerQuests(PlayerCharacter pc) {
        return playerQuests.get(pc.getUniqueCharacterId());
    }

    public Promise<IQuest> getQuest(String quest_id) {
        // Turn the Promise<Quest> into Promise<IQuest>
        return quests.get(quest_id).next(p -> Promise.make(p.orElse(null)));
    }

    // REFACTOR THIS OUT ASAP
    public Promise<IQuest> getQuestBySlug(String slug) {
        // Turn the Promise<Quest> into Promise<IQuest>
        Optional<Quest> quest = quests.asMap().values().stream().filter(v -> v.getSlug().equals(slug)).findFirst();

        if(quest.isPresent())
            return Promise.make(quest.get());

        JsonObject jo = new JsonObject();
        jo.add("slug", new JsonPrimitive(slug));
        return rpc.getQuest(jo).next(p -> {
            Quest q = p.orElse(null);

            if(q != null && quests.getIfPresent(q.getId()) == null) {
                quests.put(q.getId(), q);
            }

            return Promise.make(q);
        });
    }

    public void addPlayerQuest(PlayerCharacter pc, IQuestInstance instance) {
        playerQuests.put(pc.getUniqueCharacterId(), instance);
    }

    /**
     * Removes the quest instance for the player. This effectively resets the quest completely.
     */
    public void removeQuestProgress(IQuest quest, PlayerCharacter pc) {
        Optional<IQuestInstance> instance = quest.removeInstance(pc.getUniqueCharacterId());

        if(instance.isPresent())
            playerQuests.remove(pc.getUniqueCharacterId(), instance.get());
    }

    private Promise onLogin(final PlayerCharacter pc) {
        return rpc.getPlayerQuests(pc.getUniqueCharacterId()).onSuccess(val -> {
            Collection<QuestInstance> quests = val.orElse(ImmutableList.of())/*
                    // Filter out quests that no longer exist
                    .stream().filter(v -> v.getQuest() != null).collect(Collectors.toList())*/;

            System.out.println(quests.size());
            System.out.println(quests);

            // Set the player and add the instance to the respective quest
            quests.forEach(qi -> qi.setPlayer(pc));
            quests.forEach(qi -> qi.getQuest().setInstance(pc.getUniqueCharacterId(), qi));

            playerQuestsLoaded.putAll(pc.getUniqueCharacterId(), quests);
        });
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
    private Promise onLogout(PlayerCharacter pc) {
        // Remove the instance from quests.
        for (IQuestInstance instance : getPlayerQuests(pc))
            instance.getQuest().removeInstance(pc.getUniqueCharacterId());

        // TODO: create an API endpoint to save all quests in one query

        return rpc.savePlayerQuests(pc.getUniqueCharacterId(), playerQuests.removeAll(pc.getUniqueCharacterId()));
    }

    private Promise<Boolean> onDelete(final PlayerCharacter pc) {
        for (IQuestInstance instance : getPlayerQuests(pc))
            instance.getQuest().removeInstance(pc.getUniqueCharacterId());

        playerQuests.removeAll(pc.getUniqueCharacterId());

        return this.rpc.deletePlayerQuests(pc.getUniqueCharacterId());
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
    private Quest decodeQuest(JsonObject jo, JsonDeserializationContext context) throws IllegalAccessException {
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
                String id = nodeEntry.getKey();
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
                    node = nodeClass.getDeclaredConstructor(String.class).newInstance(id);
                } catch (Exception e) {
                    // Silence intellij. There will never be a time that this constructor is not defined.
                }

                // Decode default input interface values
                for(Map.Entry<String, JsonElement> inportEntry : obj.getAsJsonObject("inputs").entrySet()) {
                    Field f = findField(INodeInput.class, nodeClass, inportEntry.getKey());
                    if(f == null) {
                        throw new IllegalArgumentException("Unknown inport interface: " + inportEntry.getKey() + " in " + nodeClass.getSimpleName());
                    }

                    INodeInput in = (INodeInput)f.get(node);

                    if(in == null) continue;

                    // If it's a value type, then we set the default value. If it's not a value type, then it has no default,
                    // as it is likely an execution port.
                    if(in instanceof IInportValue) {
                        IInportValue inv = (IInportValue)in;
                        try {
                            inv.setDefaultValue(APIController.getInstance().getGson().fromJson(inportEntry.getValue(), inv.getValueClass()));
                        } catch(Exception e) {
                            throw new IllegalArgumentException("Failed to decode inport value: " + inportEntry.getKey() + " in " + nodeClass.getSimpleName(), e);
                        }
                    }
                }

                // Decode options
                for(Map.Entry<String, JsonElement> optionEntry : obj.getAsJsonObject("options").entrySet()) {
                    Field f = findField(Object.class, nodeClass, optionEntry.getKey());

                    if(f == null) {
                        throw new IllegalArgumentException("Unknown option: " + optionEntry.getKey() + " in " + nodeClass.getSimpleName());
                    }

                    try {
                        f.set(node, APIController.getInstance().getGson().fromJson(optionEntry.getValue(), f.getType()));
                    } catch(Exception e) {
                        throw new IllegalArgumentException("Failed to decode option value: " + optionEntry.getKey() + " in " + nodeClass.getSimpleName(), e);
                    }
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

                IQuestNode fromNode = nodes.get(from.get(0).getAsString());
                IQuestNode toNode = nodes.get(to.get(0).getAsString());

                INodeOutput out = (INodeOutput)findField(INodeOutput.class, fromNode.getClass(), from.get(1).getAsString()).get(fromNode);
                if(out == null) {
                    throw new IllegalArgumentException("Node '" + fromNode.getClass().getSimpleName() + "' must contain a(n) " + from.get(1).getAsString() + " outport!");
                }

                INodeInput in = (INodeInput)findField(INodeInput.class, toNode.getClass(), to.get(1).getAsString()).get(toNode);
                if(in == null) {
                    throw new IllegalArgumentException("Node '" + toNode.getClass().getSimpleName() + "' must contain a(n) " + to.get(1).getAsString() + " inport!");
                }

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

    private Field findField(Class<?> type, Class<?> clazz, String name) {
        Field field = null;
        try {
            field = clazz.getField(name);

            if(!Modifier.isTransient(field.getModifiers())) {
                if (type.isAssignableFrom(field.getType())) {
                    field = null;
                }
            }
        } catch (NoSuchFieldException e) {

        }

        if(field == null) {
            for(Field f : clazz.getFields()) {
                if(Modifier.isTransient(f.getModifiers())) continue;
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

        return field;
    }

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

            onLogout(event.getPlayerCharacter())
                    .on(lock::release);
        }

        @EventHandler
        public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
            onDelete(event.getPlayerCharacter());
        }
    }
}
