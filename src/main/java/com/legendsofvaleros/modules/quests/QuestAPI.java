package com.legendsofvaleros.modules.quests;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import com.google.gson.*;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.quests.action.QuestActionFactory;
import com.legendsofvaleros.modules.quests.action.QuestActions;
import com.legendsofvaleros.modules.quests.api.*;
import com.legendsofvaleros.modules.quests.core.*;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.quests.prerequisite.PrerequisiteFactory;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.PromiseCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class QuestAPI extends ModuleListener {
    public interface RPC {
        Promise<IQuest> getQuest(String questId);

        Promise<Map<String, Object>> getPlayerQuestsProgress(CharacterId characterId);
        Promise<Boolean> savePlayerQuestsProgress(CharacterId characterId, Map<String, Object> map);
        Promise<Boolean> deletePlayerQuestsProgress(CharacterId characterId);
    }

    private RPC rpc;

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private PromiseCache<String, IQuest> quests;
    public Promise<IQuest> getQuest(String quest_id) {
        return quests.get(quest_id);
    }

    // Event class, gear ID, objective list
    private HashBasedTable<Class<? extends Event>, String, Set<IQuestEventReceiver>> questEvents = HashBasedTable.create();
    private Multimap<String, InternalTask> questUpdates = HashMultimap.create();

    /**
     * A map containing each user to a list of all their accepted quests. This
     * keeps the quest in memory for as long as they remain on the server.
     */
    private Multimap<CharacterId, IQuest> playerQuests = HashMultimap.create();
    public Collection<IQuest> getPlayerQuests(PlayerCharacter pc) {
        if(!playerQuests.containsKey(pc.getUniqueCharacterId()))
            return ImmutableList.of();
        return playerQuests.get(pc.getUniqueCharacterId());
    }

    /**
     * Stores a list of completed quests, along with a LocalDateTime representation
     * of the exact time they completed the quest.
     */
    public HashBasedTable<CharacterId, String, LocalDateTime> completedQuests = HashBasedTable.create();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(QuestController.getInstance(), RPC.class);

        this.quests = new PromiseCache<>(CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakValues()
                .removalListener(entry -> {
                    QuestController.getInstance().getLogger().warning("Quest '" + entry.getKey() + "' removed from the cache: " + entry.getCause());

                    questEvents.column(String.valueOf(entry.getKey())).clear();

                    for(InternalTask task : questUpdates.get(String.valueOf(entry.getKey())))
                        task.cancel();
                    questUpdates.removeAll(String.valueOf(entry.getKey()));
                })
                .build(), this::loadQuest);

        QuestController.getInstance().getScheduler().executeInMyCircleTimer(() -> {
            // This is done so we get almost-live updates on GC'd listeners.
            quests.cleanUp();
        }, 0L, 20L);

        APIController.getInstance().getGsonBuilder()
            .registerTypeAdapter(IQuest.class, (JsonDeserializer<IQuest>) (json, typeOfT, context) -> {
                Class<? extends IQuest> questClass = QuestFactory.getType(
                        json.getAsJsonObject().get("type").getAsString());
                return context.deserialize(json, questClass);
            })
            .registerTypeAdapter(QuestPrereqMap.class, (JsonDeserializer<QuestPrereqMap>) (json, typeOfT, context) -> {
                QuestPrereqMap loader = new QuestPrereqMap();
                JsonObject obj = json.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    try {
                        Class<? extends IQuestPrerequisite> prereq = PrerequisiteFactory.getType(entry.getKey());
                        if (prereq == null)
                            throw new RuntimeException("Unknown prerequisite type! Offender: " + entry.getKey());
                        loader.put(entry.getKey(), context.deserialize(entry.getValue(), prereq));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to decode prerequisite! " + (e.getMessage() != null ? e.getMessage() : e.getCause().toString()) + " Offender: " + entry.getKey() + " (" + entry.getValue() + ")");
                    }
                }
                return loader;
            })
            .registerTypeAdapter(IQuestAction.class, (JsonDeserializer<IQuestAction>) (json, typeOfT, context) -> {
                try {
                    JsonObject obj = json.getAsJsonObject();

                    if (obj.get("type") == null || obj.get("type").getAsString() == null)
                        throw new JsonParseException("No type defined in action. Offender: " + obj.toString());

                    return context.deserialize(obj.get("fields").getAsJsonObject(), QuestActionFactory.getType(obj.get("type").getAsString()));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to decode action! " + (e.getMessage() != null ? e.getMessage() : (e.getCause() != null ? e.getCause() : e)) + " (" + json.toString() + ")");
                }
            })
            .registerTypeAdapter(IQuestObjective.class, (JsonDeserializer<IQuestObjective<?>>) (json, typeOfT, context) -> {
                try {
                    JsonObject obj = json.getAsJsonObject();

                    if (obj.get("type").getAsString() == null)
                        throw new JsonParseException("No type defined in action. Offender: " + obj.toString());

                    return context.deserialize(obj.get("fields").getAsJsonObject(), QuestObjectiveFactory.getType(obj.get("type").getAsString()));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to decode objective! " + (e.getMessage() != null ? e.getMessage() : e.getCause().toString()) + " (" + json.toString() + ")");
                }
            })
            .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, typeOfT, context) -> Duration.parse(json.getAsString()));

        registerEvents(new PlayerListener());
    }

    public Promise<Void> onLogin(final PlayerCharacter pc) {
        Promise<Void> promise = new Promise<>();

        rpc.getPlayerQuestsProgress(pc.getUniqueCharacterId()).onSuccess(val -> {
            Map<String, Object> map = val.orElse(ImmutableMap.of());

            if(map.size() == 0) {
                promise.resolve(null);
                return;
            }

            AtomicInteger questsToLoad = new AtomicInteger(0);

            map.forEach((k, v) -> {
                getQuest(k).onSuccess(q -> {
                    if(q.isPresent()) {
                        try {
                            IQuest quest = q.get();

                            // Quest is already completed.
                            if (v instanceof JsonArray) {
                                JsonArray arr = (JsonArray) v;
                                completedQuests.put(pc.getUniqueCharacterId(),
                                        arr.get(0).getAsString(),
                                        LocalDateTime.parse(arr.get(1).getAsString(), DATE_FORMAT));
                            } else {
                                JsonObject progressObj = (JsonObject) v;

                                JsonArray progresses = progressObj.get("data").getAsJsonArray();
                                QuestProgressPack progressPack = new QuestProgressPack(
                                        progressObj.has("group") ? progressObj.get("group").getAsInt() : null,
                                        progresses.size());

                                progressPack.actionI = progressObj.has("actionI") ? progressObj.get("actionI").getAsInt() : null;

                                IQuestObjective<?>[] objectiveGroup = quest.getObjectives().groups[progressPack.group];

                                for (int i = 0; i < progresses.size(); i++) {
                                    JsonElement entry = progresses.get(i);

                                    // Decode old quest progress system
                                    // NOTE: Until this is removed, complex progress objects cannot add the "type" value.
                                    if (entry.isJsonObject() && entry.getAsJsonObject().has("type")) {
                                        JsonObject oldProg = entry.getAsJsonObject();
                                        if (oldProg.get("type").getAsString().equals("bool"))
                                            progressPack.setForObjective(i, oldProg.get("progress").getAsJsonObject().get("value").getAsBoolean());
                                        if (oldProg.get("type").getAsString().equals("int"))
                                            progressPack.setForObjective(i, oldProg.get("progress").getAsJsonObject().get("value").getAsInt());
                                        continue;
                                    }

                                    progressPack.setForObjective(i,
                                            APIController.getInstance().getGson().fromJson(entry,
                                                    objectiveGroup[i].getProgressClass()));
                                }

                                quest.loadProgress(pc, progressPack);
                                playerQuests.put(pc.getUniqueCharacterId(), quest);
                            }
                        } catch (Exception e) {
                            QuestController.getInstance().getLogger().warning("Player attempt to load progress for quest, but something went wrong. Offender: " + pc.getPlayer().getName() + " in quest " + k);
                            MessageUtil.sendSevereException(QuestController.getInstance(), pc.getPlayer(), e);
                        }
                    }

                    if(questsToLoad.incrementAndGet() == 0)
                        promise.resolve(null);
                });
            });
        });

        return promise;
    }

    /**
     * Save player quest progress to database.
     * @param pc The player.
     */
    public Promise<Boolean> onLogout(PlayerCharacter pc) {
        Map<String, Object> map = new HashMap<>();

        for(Map.Entry<String, LocalDateTime> entry : completedQuests.row(pc.getUniqueCharacterId()).entrySet())
            map.put(entry.getKey(), new String[] { "done", entry.getValue().format(DATE_FORMAT) });

        for(IQuest quest : playerQuests.get(pc.getUniqueCharacterId())) {
            map.put(quest.getId(), quest.getProgress(pc));
        }

        return this.rpc.savePlayerQuestsProgress(pc.getUniqueCharacterId(), map);
    }

    public Promise<Boolean> onDelete(final PlayerCharacter pc) {
        return this.rpc.deletePlayerQuestsProgress(pc.getUniqueCharacterId());
    }

    public void addPlayerQuest(PlayerCharacter pc, IQuest quest) {
        playerQuests.put(pc.getUniqueCharacterId(), quest);
    }

    /**
     * Completely remove quest progress. Both from memory, and from the database. The player will be able to redo the quest
     * as if they hadn't done it in the first place.
     * @param quest_id The quest ID to remove.
     * @param pc       The player.
     */
    public void removeQuestProgress(String quest_id, PlayerCharacter pc) {
        if (quest_id.equals("*")) {
            for (IQuest q : playerQuests.values())
                q.clearProgress(pc);

            playerQuests.removeAll(pc.getUniqueCharacterId());
            completedQuests.row(pc.getUniqueCharacterId()).clear();
            return;
        }

        IQuest q = quests.getIfPresent(quest_id);
        if (q != null) {
            playerQuests.remove(pc.getUniqueCharacterId(), q);
            q.clearProgress(pc);
        }

        completedQuests.remove(pc.getUniqueCharacterId(), quest_id);
    }

    /**
     * Set a player's quest to finished in the database.
     * @param quest The quest object.
     * @param pc    The player.
     */
    public void finishQuest(IQuest quest, PlayerCharacter pc) {
        playerQuests.remove(pc.getUniqueCharacterId(), quest);
        completedQuests.put(pc.getUniqueCharacterId(), quest.getId(), LocalDateTime.now());
    }

    private Promise<IQuest> loadQuest(String questId) {
        return rpc.getQuest(questId).onSuccess(val -> {
            if(!val.isPresent()) return;

            IQuest quest = val.get();

            HashMultimap<Class<? extends Event>, IQuestEventReceiver> eventMap = HashMultimap.create();

            WeakReference<IQuest> ref = new WeakReference<>(quest);

            {
                QuestActions actions = quest.getActions();

                for (int i = 0; i < actions.accept.length; i++)
                    actions.accept[i].init(ref, null, i);

                for (int i = 0; i < actions.decline.length; i++)
                    actions.decline[i].init(ref, null, i);

                for (int group = 0; group < actions.groups.length; group++)
                    for (int i = 0; i < actions.groups[group].length; i++)
                        actions.groups[group][i].init(ref, group, i);
            }

            {
                QuestObjectives objectives = quest.getObjectives();

                for (int group = 0; group < objectives.groups.length; group++)
                    for (int i = 0; i < objectives.groups[group].length; i++)
                        objectives.groups[group][i].init(ref, group, i);
            }

            for (IQuestObjective<?>[] group : quest.getObjectives().groups)
                for (IQuestObjective<?> obj : group) {
                    Class<? extends Event>[] requested = obj.getRequestedEvents();
                    if (requested == null) continue;
                    for (Class<? extends Event> e : requested)
                        eventMap.put(e, obj);
                }

            for (IQuestAction[] acts : quest.getActions().getAll())
                for (IQuestAction act : acts) {
                    if(!(act instanceof IQuestEventReceiver)) continue;

                    Class<? extends Event>[] requested = ((IQuestEventReceiver)act).getRequestedEvents();
                    if (requested == null) continue;
                    for (Class<? extends Event> e : requested)
                        eventMap.put(e, (IQuestEventReceiver)act);
                }

            for (Class<? extends Event> event : eventMap.keySet())
                questEvents.put(event, questId, eventMap.get(event));

            for (IQuestObjective<?>[] group : quest.getObjectives().groups)
                for (IQuestObjective<?> obj : group) {
                    int timer = obj.getUpdateTimer();
                    if (timer <= 0) continue;

                    questUpdates.put(questId, QuestController.getInstance().getScheduler().executeInSpigotCircleTimer(
                            new QuestUpdater(quest, obj),
                            0L, timer));
                }
        });
    }

    public void reloadQuests() throws Throwable {
        for (Map.Entry<CharacterId, IQuest> entry : playerQuests.entries())
            onLogout(Characters.getPlayerCharacter(entry.getKey())).get();

        playerQuests.clear();

        quests.invalidateAll();
        quests.cleanUp();

        if (quests.size() > 0)
            MessageUtil.sendException(QuestController.getInstance(), quests.size() + " quests did not get cleared from the cache.");

        for (Player p : Bukkit.getOnlinePlayers()) {
            if(!Characters.isPlayerCharacterLoaded(p)) continue;
            onLogin(Characters.getPlayerCharacter(p)).get();
        }
    }

    public QuestStatus getStatus(PlayerCharacter pc, IQuest quest) {
        if (quest == null)
            return QuestStatus.NONE;

        if (completedQuests.contains(pc.getUniqueCharacterId(), quest.getId()))
            if (!quest.isRepeatable()) {
                return QuestStatus.COMPLETED;
            } else {
                for (IQuestPrerequisite prereq : quest.getPrerequisites())
                    if (!prereq.canRepeat(quest, pc))
                        return QuestStatus.REPEATABLE_NOT_READY;
                return QuestStatus.REPEATABLE_READY;
            }

        if (quest.hasProgress(pc))
            return QuestStatus.ACCEPTED;

        for (IQuestPrerequisite prereq : quest.getPrerequisites())
            if (!prereq.canAccept(quest, pc))
                return QuestStatus.PREREQ;

        return QuestStatus.NEITHER;
    }

    public synchronized void callEvent(Event event, PlayerCharacter pc) {
        for (Map.Entry<String, Set<IQuestEventReceiver>> entry : questEvents.row(event.getClass()).entrySet()) {
            IQuest quest = quests.getIfPresent(entry.getKey());
            if (quest == null) continue;

            QuestProgressPack progress = quest.getProgress(pc);
            if(progress == null) continue;

            for (IQuestEventReceiver receiver : entry.getValue()) {
                if(receiver instanceof IQuestObjective)
                    if(progress.group == null || ((IQuestObjective)receiver).getGroupIndex() != progress.group)
                        continue;
                    else if(receiver instanceof IQuestAction) {
                        if(progress.group == null || ((IQuestAction) receiver).getGroupIndex() != progress.group)
                            continue;
                        if(progress.actionI == null || ((IQuestAction) receiver).getActionIndex() != progress.actionI)
                            continue;
                    }

                receiver.onEvent(event, pc);
            }

            // Check if the quest has been completed.
            quest.checkCompleted(pc);
        }
    }

    private static class QuestPrereqMap extends HashMap<String, IQuestPrerequisite> {
        private static final long serialVersionUID = 1L;
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Quests");

            onLogin(event.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler
        public void onCharacterFinishLoading(PlayerCharacterFinishLoadingEvent event) {
            // Resume actions if they were paused
            for (IQuest quest : getPlayerQuests(event.getPlayerCharacter()))
                quest.testResumeActions(event.getPlayerCharacter());
        }

        @EventHandler
        public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Quests");

            onLogout(event.getPlayerCharacter()).on(lock::release);

            for (IQuest q : getPlayerQuests(event.getPlayerCharacter())) {
                q.saveProgress(event.getPlayerCharacter());
                q.clearProgress(event.getPlayerCharacter());
            }

            playerQuests.removeAll(event.getPlayerCharacter().getUniqueCharacterId());
            completedQuests.row(event.getPlayerCharacter().getUniqueCharacterId()).clear();
        }

        @EventHandler
        public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
            onDelete(event.getPlayerCharacter());
        }
    }
}
