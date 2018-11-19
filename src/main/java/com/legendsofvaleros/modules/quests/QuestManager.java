package com.legendsofvaleros.modules.quests;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.modules.quests.action.stf.ActionFactory;
import com.legendsofvaleros.modules.quests.action.stf.QuestActions;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.objective.stf.ObjectiveFactory;
import com.legendsofvaleros.modules.quests.prerequisite.stf.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.prerequisite.stf.PrerequisiteFactory;
import com.legendsofvaleros.modules.quests.progress.stf.IObjectiveProgress;
import com.legendsofvaleros.modules.quests.progress.stf.ObjectiveProgressPack;
import com.legendsofvaleros.modules.quests.progress.stf.ProgressFactory;
import com.legendsofvaleros.modules.quests.progress.stf.QuestProgressPack;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.quests.quest.stf.QuestFactory;
import com.legendsofvaleros.modules.quests.quest.stf.QuestObjectives;
import com.legendsofvaleros.modules.quests.quest.stf.QuestStatus;
import com.legendsofvaleros.util.FutureCache;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class QuestManager {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String QUEST_ID = "quest_id";

    private static final String PLAYER_QUESTS_TABLE = "player_quests_progress";
    private static final String CHARACTER_FIELD = "character_id";
    private static final String QUEST_DATE = "quest_date";
    private static final String QUEST_PROGRESS = "quest_progress";


    private static final String QUEST_TABLE = "quests";
    private static final String QUEST_GROUP = "quest_group";
    private static final String QUEST_AUTHOR = "quest_author";

    private static final String QUEST_TYPE = "quest_type";
    private static final String QUEST_NAME = "quest_name";
    private static final String QUEST_DESCRIPTION = "quest_description";

    private static final String QUEST_FORCED = "quest_forced";
    private static final String QUEST_REPEAT = "quest_repeat";

    private static final String QUEST_PREREQS = "quest_prereqs";
    private static final String QUEST_ACTIONS = "quest_actions";
    private static final String QUEST_OBJECTIVES = "quest_objectives";

    private static Gson gson;

    private static TableManager managerPlayers;
    private static TableManager managerQuests;

    private static FutureCache<String, IQuest> quests;

    public static ListenableFuture<IQuest> getQuest(String quest_id) {
        return quests.get(quest_id);
    }

    // Event class, quest ID, objective list
    private static HashBasedTable<Class<? extends Event>, String, Set<IObjective>> questEvents = HashBasedTable.create();

    /**
     * A map containing each user to a list of all their accepted quests. This
     * keeps the quest in memory for as long as they remain on the server.
     */
    private static Multimap<CharacterId, IQuest> playerQuests = HashMultimap.create();

    public static Collection<IQuest> getQuestsForEntity(PlayerCharacter pc) {
        return playerQuests.get(pc.getUniqueCharacterId());
    }

    /**
     * Stores a list of completed quests, along with a LocalDateTime representation
     * of the exact time they completed the quest.
     */
    public static HashBasedTable<CharacterId, String, LocalDateTime> completedQuests = HashBasedTable.create();

    private static void applyFields(String act, Object obj, JsonObject fields) {
        Map<String, Field> classFields = getFields(obj.getClass());
        for (Entry<String, JsonElement> entry : fields.entrySet()) {
            try {
                Field field = classFields.get(entry.getKey());
                if (field == null) continue;

                field.setAccessible(true);

                field.set(obj, gson.getAdapter(field.getType()).fromJsonTree(entry.getValue()));
            } catch (Exception e) {
                MessageUtil.sendException(Quests.getInstance(), null, new Exception("Failed to apply fields! Offender: " + (obj == null ? "null" : obj.getClass().getSimpleName()) + ":" + act), true);
            }
        }
    }

    private static Map<String, Field> getFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<>();
        while (clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields())
                fields.put(f.getName(), f);
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static void onEnable() {
        gson = new GsonBuilder()
                .registerTypeAdapter(QuestPrereqLoader.class, (JsonDeserializer<QuestPrereqLoader>) (json, typeOfT, context) -> {
                    QuestPrereqLoader loader = new QuestPrereqLoader();
                    JsonObject obj = json.getAsJsonObject();
                    for (Entry<String, JsonElement> entry : obj.entrySet()) {
                        try {
                            Class<? extends IQuestPrerequisite> prereq = PrerequisiteFactory.getType(entry.getKey());
                            if (prereq == null)
                                throw new RuntimeException("Unknown prerequisite type! Offender: " + entry.getKey());
                            loader.put(entry.getKey(), gson.fromJson(entry.getValue(), prereq));
                        } catch (Exception e) {
                            if (e instanceof RuntimeException) throw e;
                            throw new RuntimeException("Failed to decode prerequisite! " + (e.getMessage() != null ? e.getMessage() : e.getCause().toString()) + " Offender: " + entry.getKey() + " (" + entry.getValue() + ")");
                        }
                    }
                    return loader;
                })
                .registerTypeAdapter(AbstractAction.class, (JsonDeserializer<AbstractAction>) (json, typeOfT, context) -> {
                    try {
                        JsonObject obj = json.getAsJsonObject();

                        if (obj.get("type") == null || obj.get("type").getAsString() == null)
                            throw new JsonParseException("No type defined in action. Offender: " + obj.toString());

                        AbstractAction act = ActionFactory.newAction(obj.get("type").getAsString());
                        applyFields(obj.get("type").getAsString(), act, obj.get("fields").getAsJsonObject());
                        act.onInit();
                        return act;
                    } catch (Exception e) {
                        if (e instanceof RuntimeException) throw e;
                        throw new RuntimeException("Failed to decode action! " + (e.getMessage() != null ? e.getMessage() : e.getCause().toString()) + " (" + json.toString() + ")");
                    }
                })
                .registerTypeAdapter(IObjective.class, (JsonDeserializer<IObjective<?>>) (json, typeOfT, context) -> {
                    try {
                        JsonObject obj = json.getAsJsonObject();

                        if (obj.get("type").getAsString() == null)
                            throw new JsonParseException("No type defined in action. Offender: " + obj.toString());

                        IObjective<?> objective = ObjectiveFactory.newObjective(obj.get("type").getAsString());
                        applyFields(obj.get("type").getAsString(), objective, obj.get("fields").getAsJsonObject());
                        return objective;
                    } catch (Exception e) {
                        if (e instanceof RuntimeException) throw e;
                        throw new RuntimeException("Failed to decode objective! " + (e.getMessage() != null ? e.getMessage() : e.getCause().toString()) + " (" + json.toString() + ")");
                    }
                })
                .registerTypeAdapter(ObjectiveProgressPack.class, (JsonDeserializer<ObjectiveProgressPack>) (json, typeOfT, context) -> {
                    try {
                        JsonObject obj = json.getAsJsonObject();

                        if (obj.get("type").getAsString() == null)
                            throw new JsonParseException("No type defined in action. Offender: " + obj.toString());

                        IObjectiveProgress progress = ProgressFactory.forType(obj.get("type").getAsString());
                        applyFields(obj.get("type").getAsString(), progress, obj.get("progress").getAsJsonObject());
                        return new ObjectiveProgressPack(progress);
                    } catch (Exception e) {
                        if (e instanceof RuntimeException) throw e;
                        throw new RuntimeException("Failed to player progress! " + (e.getMessage() != null ? e.getMessage() : e.getCause().toString()) + " (" + json.toString() + ")");
                    }
                })
                .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, typeOfT, context) -> Duration.parse(json.getAsString()))
                .create();

        managerPlayers = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), PLAYER_QUESTS_TABLE);

        managerPlayers.primary(CHARACTER_FIELD, "VARCHAR(38)")
                .primary(QUEST_ID, "VARCHAR(64)")
                .column(QUEST_DATE, "DATETIME")
                .column(QUEST_PROGRESS, "TEXT").create();

        managerQuests = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), QUEST_TABLE);

        managerQuests.primary(QUEST_ID, "VARCHAR(64)")
                .column(QUEST_GROUP, "VARCHAR(64)")
                .column(QUEST_AUTHOR, "INT")

                .column(QUEST_TYPE, "VARCHAR(16)")
                .column(QUEST_NAME, "VARCHAR(64)")
                .column(QUEST_DESCRIPTION, "VARCHAR(512)")

                .column(QUEST_FORCED, "TINYINT(1)")
                .column(QUEST_REPEAT, "TINYINT(1)")

                .column(QUEST_PREREQS, "TEXT")

                .column(QUEST_ACTIONS, "TEXT")
                .column(QUEST_OBJECTIVES, "TEXT").create();

        Quests.getInstance().registerEvents(new PlayerListener());

        quests = new FutureCache<>(CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakValues()
                .removalListener(QuestManager::onQuestUnCached)
                .build(), QuestManager::loadQuest);
    }

    public static void reloadQuests() {
        for (Entry<CharacterId, IQuest> entry : playerQuests.entries())
            saveQuestProgress(Characters.getPlayerCharacter(entry.getKey()), entry.getValue());

        playerQuests.clear();

        quests.invalidateAll();
        quests.cleanUp();

        if (quests.size() > 0)
            MessageUtil.sendException(Quests.getInstance(), null, new Exception(quests.size() + " quests did not get cleared from the cache."), false);

        for (Player p : Bukkit.getOnlinePlayers())
            loadQuestsForPlayer(Characters.getPlayerCharacter(p), null);
    }

    private synchronized static void onQuestUnCached(Entry<String, IQuest> entry) {
        Quests.getInstance().getLogger().info("Quest '" + entry.getKey() + "' removed from the cache.");

        questEvents.column(String.valueOf(entry.getKey())).clear();
    }

    public synchronized static void callEvent(Event event, PlayerCharacter pc) {
        for (Entry<String, Set<IObjective>> entry : questEvents.row(event.getClass()).entrySet()) {
            IQuest quest = quests.getIfPresent(entry.getKey());
            if (quest == null) continue;

            if (!quest.hasProgress(pc)) continue;
            int group = quest.getCurrentGroupI(pc);

            for (IObjective objective : entry.getValue()) {
                if (objective.getGroupIndex() != group) continue;
                objective.onEvent(event, pc);
            }

            // Check if the quest has been completed.
            quest.checkCompleted(pc);
        }
    }

    public static void loadQuestsForPlayer(final PlayerCharacter pc, final PhaseLock lock) {
        managerPlayers.query()
                .select()
                .where(CHARACTER_FIELD, pc.getUniqueCharacterId().toString())
                .build()
                .callback((result) -> {
                    final AtomicInteger questsToLoad = new AtomicInteger(0);

                    while (result.next()) {
                        if (result.getString(QUEST_PROGRESS).equals("done")) {
                            String time = result.getString(QUEST_DATE);
                            time = time.split("\\.")[0];
                            completedQuests.put(pc.getUniqueCharacterId(), result.getString(QUEST_ID), LocalDateTime.parse(time, DATE_FORMAT));
                        } else {
                            questsToLoad.addAndGet(1);

                            String questId = result.getString(QUEST_ID);
                            QuestProgressPack progressPack = gson.fromJson(result.getString(QUEST_PROGRESS), QuestProgressPack.class);
                            ListenableFuture<IQuest> future = getQuest(questId);
                            future.addListener(() -> {
                                try {
                                    IQuest quest = future.get();
                                    playerQuests.put(pc.getUniqueCharacterId(), quest);
                                    quest.loadProgress(pc, progressPack);
                                } catch (Exception e) {
                                    Quests.getInstance().getLogger().warning("Player attempt to load progress for quest, but something went wrong. Offender: " + pc.getPlayer().getName() + " in quest " + questId);
                                    MessageUtil.sendException(Quests.getInstance(), pc.getPlayer(), e, true);
                                }

                                int i = questsToLoad.decrementAndGet();

                                if (i == 0) {
                                    if (lock != null)
                                        lock.release();
                                }
                            }, Utilities.asyncExecutor());
                        }
                    }

                    // If there were no quests, release the lock immediately.
                    if (questsToLoad.get() == 0) {
                        if (lock != null)
                            lock.release();
                    }
                })
                .execute(true);
    }

    public static void addPlayerQuest(PlayerCharacter pc, IQuest quest) {
        playerQuests.put(pc.getUniqueCharacterId(), quest);
    }

    /**
     * Completely remove quest progress. Both from memory, and from the database. The player will be able to redo the quest
     * as if they hadn't done it in the first place.
     * @param quest_id The quest ID to remove.
     * @param pc       The player.
     */
    public static void removeQuestProgress(String quest_id, PlayerCharacter pc) {
        if (quest_id.equals("*")) {
            for (IQuest q : playerQuests.values())
                q.clearProgress(pc);

            playerQuests.removeAll(pc.getUniqueCharacterId());
            completedQuests.row(pc.getUniqueCharacterId()).clear();

            managerPlayers.query()
                    .remove()
                    .where(CHARACTER_FIELD, pc.getUniqueCharacterId().toString())
                    .build()
                    .execute(true);
            return;
        }

        IQuest q = quests.getIfPresent(quest_id);
        if (q != null) {
            playerQuests.remove(pc.getUniqueCharacterId(), q);
            q.clearProgress(pc);
        }

        completedQuests.remove(pc.getUniqueCharacterId(), quest_id);

        managerPlayers.query()
                .remove()
                .where(CHARACTER_FIELD, pc.getUniqueCharacterId().toString(),
                        QUEST_ID, quest_id)
                .limit(1)
                .build()
                .execute(true);
    }

    /**
     * Save player quest progress to database.
     * @param pc The player.
     */
    public static void saveQuestProgress(PlayerCharacter pc, IQuest q) {
        managerPlayers.query()
                .insert()
                .values(CHARACTER_FIELD, pc.getUniqueCharacterId().toString(),
                        QUEST_ID, q.getId(),
                        QUEST_DATE, LocalDateTime.now().format(DATE_FORMAT),
                        QUEST_PROGRESS, gson.toJson(q.getProgress(pc)))
                .onDuplicateUpdate(QUEST_PROGRESS)
                .build()
                .execute(true);
    }

    /**
     * Set a player's quest to finished in the database.
     * @param quest The quest object.
     * @param pc    The player.
     */
    public static void finishQuest(IQuest quest, PlayerCharacter pc) {
        LocalDateTime time = LocalDateTime.now();

        playerQuests.remove(pc.getUniqueCharacterId(), quest);
        completedQuests.put(pc.getUniqueCharacterId(), quest.getId(), time);

        managerPlayers.query()
                .insert()
                .values(CHARACTER_FIELD, pc.getUniqueCharacterId().toString(),
                        QUEST_ID, quest.getId(),
                        QUEST_DATE, time.format(DATE_FORMAT),
                        QUEST_PROGRESS, "done")
                .onDuplicateUpdate(QUEST_PROGRESS)
                .build()
                .execute(true);
    }

    public static QuestStatus getStatus(PlayerCharacter pc, IQuest quest) {
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

    private static void loadQuest(String quest_id, SettableFuture<IQuest> ret) {
        managerQuests.query()
                .select()
                .where(QUEST_ID, quest_id)
                .limit(1)
                .build()
                .callback((result) -> {
                    if (!result.next()) {
                        ret.set(null);
                        return;
                    }

                    String id = null;
                    try {
                        id = result.getString(QUEST_ID);
                        String type = result.getString(QUEST_TYPE);
                        String name = result.getString(QUEST_NAME);
                        String description = result.getString(QUEST_DESCRIPTION);
                        boolean forceAccept = result.getBoolean(QUEST_FORCED);
                        boolean repeatable = result.getBoolean(QUEST_REPEAT);
                        String prereqs = result.getString(QUEST_PREREQS);
                        String actions = result.getString(QUEST_ACTIONS);
                        String objectives = result.getString(QUEST_OBJECTIVES);

                        IQuest quest = QuestFactory.newQuest(id, type);

                        quest.setName(name);
                        quest.setDescription(description);
                        quest.setForced(forceAccept);

                        quest.setRepeatable(repeatable);

                        if (prereqs != null && prereqs.length() != 0) {
                            for (IQuestPrerequisite prereq : gson.fromJson(prereqs, QuestPrereqLoader.class).values())
                                quest.addPrerequisite(prereq);
                        }

                        if (actions != null && actions.length() != 0)
                            quest.setActions(gson.fromJson(actions, QuestActions.class));

                        if (objectives != null && actions.length() != 0)
                            quest.setObjectives(gson.fromJson(objectives, QuestObjectives.class));

                        {
                            HashMultimap<Class<? extends Event>, IObjective> eventMap = HashMultimap.create();

                            for (IObjective<?>[] group : quest.getObjectives().groups)
                                for (IObjective<?> obj : group) {
                                    Class<? extends Event>[] requested = obj.getRequestedEvents();
                                    if (requested == null) continue;
                                    for (Class<? extends Event> e : requested)
                                        eventMap.put(e, obj);
                                }

                            for (Class<? extends Event> event : eventMap.keySet())
                                questEvents.put(event, quest_id, eventMap.get(event));
                        }

                        ret.set(quest);
                    } catch (Exception e) {
                        Quests.getInstance().getLogger().severe("Failed to load quest. Offender: " + id);
                        MessageUtil.sendException(Quests.getInstance(), null, e, true);
                        ret.set(null);
                    }
                })
                .execute(true);
    }

    private static class PlayerListener implements Listener {
        @EventHandler
        public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
            loadQuestsForPlayer(event.getPlayerCharacter(), event.getLock());
        }

        @EventHandler
        public void onCharacterFinishLoading(PlayerCharacterFinishLoadingEvent event) {
            // Resume actions if they were paused
            for (IQuest quest : getQuestsForEntity(event.getPlayerCharacter()))
                quest.testResumeActions(event.getPlayerCharacter());
        }

        @EventHandler
        public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
            removeQuestProgress("*", event.getPlayerCharacter());
        }

        @EventHandler
        public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
            for (IQuest q : getQuestsForEntity(event.getPlayerCharacter())) {
                q.saveProgress(event.getPlayerCharacter());
                q.clearProgress(event.getPlayerCharacter());
            }

            playerQuests.removeAll(event.getPlayerCharacter().getUniqueCharacterId());
            completedQuests.row(event.getPlayerCharacter().getUniqueCharacterId()).clear();
        }
    }

    private static class QuestPrereqLoader extends HashMap<String, IQuestPrerequisite> {
        private static final long serialVersionUID = 1L;
    }
}