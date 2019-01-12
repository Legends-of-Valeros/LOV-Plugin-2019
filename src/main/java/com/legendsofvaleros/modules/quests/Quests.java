package com.legendsofvaleros.modules.quests;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterCreateEvent;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.quests.action.*;
import com.legendsofvaleros.modules.quests.action.stf.QuestActionFactory;
import com.legendsofvaleros.modules.quests.event.QuestCompletedEvent;
import com.legendsofvaleros.modules.quests.event.QuestObjectivesStartedEvent;
import com.legendsofvaleros.modules.quests.event.QuestStartedEvent;
import com.legendsofvaleros.modules.quests.objective.DummyObjective;
import com.legendsofvaleros.modules.quests.objective.InteractBlockObjective;
import com.legendsofvaleros.modules.quests.objective.ReturnObjective;
import com.legendsofvaleros.modules.quests.objective.TalkObjective;
import com.legendsofvaleros.modules.quests.objective.stf.QuestObjectiveFactory;
import com.legendsofvaleros.modules.quests.prerequisite.*;
import com.legendsofvaleros.modules.quests.prerequisite.stf.PrerequisiteFactory;
import com.legendsofvaleros.modules.quests.progress.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.quests.progress.QuestObjectiveProgressInteger;
import com.legendsofvaleros.modules.quests.progress.stf.ProgressFactory;
import com.legendsofvaleros.modules.quests.quest.BasicQuest;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.quests.quest.stf.QuestFactory;
import com.legendsofvaleros.modules.quests.quest.stf.QuestStatus;
import com.legendsofvaleros.modules.quests.trait.TraitQuestGiver;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import com.sun.xml.internal.ws.api.message.Message;
import io.chazza.advancementapi.AdvancementAPI;
import io.chazza.advancementapi.FrameType;
import io.chazza.advancementapi.Trigger;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.concurrent.ExecutionException;

@DependsOn(NPCs.class)
@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
public class Quests extends ModuleListener {
    public static AdvancementAPI NEW_OBJECTIVES;

    private static Quests instance;
    public static Quests getInstance() { return instance; }

    private String introQuestId;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        introQuestId = getConfig().getString("intro-quest", "intro");

        QuestManager.onEnable();
        ActiveTracker.onEnable();
        NPCs.registerTrait("questgiver", TraitQuestGiver.class);

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new QuestCommands());

        registerEvents(new TraitQuestGiver.Marker());

        getLogger().info("is registering quests");
        {
            QuestFactory.registerType("basic", BasicQuest.class);
        }

        getLogger().info("is registering prerequisites");
        PrerequisiteFactory.registerType("class", ClassPrerequisite.class);
        PrerequisiteFactory.registerType("race", RacePrerequisite.class);
        PrerequisiteFactory.registerType("level", LevelPrerequisite.class);
        PrerequisiteFactory.registerType("quests", QuestsPrerequisite.class);
        PrerequisiteFactory.registerType("time", TimePrerequisite.class);

        getLogger().info("is registering progress loaders");
        ProgressFactory.registerType("int", QuestObjectiveProgressInteger.class);
        ProgressFactory.registerType("bool", QuestObjectiveProgressBoolean.class);

        getLogger().info("is registering objectives");
        QuestObjectiveFactory.registerType("dummy", DummyObjective.class);
        QuestObjectiveFactory.registerType("talk", TalkObjective.class);
        QuestObjectiveFactory.registerType("return", ReturnObjective.class);
        QuestObjectiveFactory.registerType("interact_block", InteractBlockObjective.class);

        getLogger().info("is registering actions");
        QuestActionFactory.registerType("conversation", ActionConversation.class);
        QuestActionFactory.registerType("goto", ActionGoTo.class);

        QuestActionFactory.registerType("command_run", ActionRunCommand.class);
        QuestActionFactory.registerType("speech", ActionSpeech.class);
        QuestActionFactory.registerType("wait", ActionWait.class);

        QuestActionFactory.registerType("quest_new", ActionNewQuest.class);

        QuestActionFactory.registerType("notify", ActionNotification.class);

        QuestActionFactory.registerType("text", ActionText.class);

        QuestActionFactory.registerType("particle", ActionParticle.class);
        QuestActionFactory.registerType("sound", ActionSound.class);
        QuestActionFactory.registerType("xp", ActionExperience.class);
        QuestActionFactory.registerType("teleport", ActionTeleport.class);
        QuestActionFactory.registerType("show_credits", ActionShowCredits.class);

        InventoryManager.addFixedItem(42, new InventoryManager.InventoryItem(null,
                (p, event) -> p.performCommand("quests gui")));
        InventoryManager.addFixedItem(43, new InventoryManager.InventoryItem(null,
                (p, event) -> p.performCommand("quests gui")));

        getLogger().info("is registering advancements.");
        NEW_OBJECTIVES = AdvancementAPI.builder(new NamespacedKey(LegendsOfValeros.getInstance(), "quests/new_objectives"))
                    .title("New Objectives")
                    .description("See quest book for details.")
                    .icon("minecraft:paper")
                    .trigger(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "impossible"))
                    .hidden(true)
                    .toast(true)
                    .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
                    .frame(FrameType.TASK)
                .build();
        NEW_OBJECTIVES.add();
    }

    @Override
    public void onUnload() {
        super.onUnload();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!Characters.isPlayerCharacterLoaded(p)) continue;
            PlayerCharacter pc = Characters.getPlayerCharacter(p);

            for (IQuest q : QuestManager.getQuestsForEntity(pc))
                q.saveProgress(pc);
        }
    }

    public static ListenableFuture<Boolean> attemptGiveQuest(PlayerCharacter pc, String questId) {
        SettableFuture<Boolean> ret = SettableFuture.create();

        ListenableFuture<IQuest> future = QuestManager.getQuest(questId);
        future.addListener(() -> {
            try {
                IQuest quest = future.get();

                if (quest != null) {
                    QuestStatus status = QuestManager.getStatus(pc, quest);

                    if (status.canAccept()) {
                        QuestManager.removeQuestProgress(quest.getId(), pc);

                        QuestManager.addPlayerQuest(pc, quest);

                        quest.onStart(pc);

                        ret.set(true);

                        return;
                    }
                }
            } catch (Exception e) {
                MessageUtil.sendException(Quests.getInstance(), pc.getPlayer(), e, true);
            }

            ret.set(false);
        }, Quests.getInstance().getScheduler()::async);

        return ret;
    }

    @EventHandler
    public void onQuestStarted(QuestStartedEvent event) {
        Title title = new Title("New Quest", event.getQuest().getName(), 10, 40, 10);
        title.setTimingsToTicks();
        title.setTitleColor(ChatColor.GOLD);
        TitleUtil.queueTitle(title, event.getPlayer());
    }

    @EventHandler
    public void onQuestComplete(QuestCompletedEvent event) {
        event.getPlayer().playSound(event.getPlayer().getLocation(), "misc.questcomplete", 1F, 1F);
    }

    @EventHandler
    public void onNewObjectives(QuestObjectivesStartedEvent event) {
        if(NEW_OBJECTIVES != null)
        	NEW_OBJECTIVES.show(event.getPlayer());
    }

    @EventHandler
    public void onCharacterCreated(PlayerCharacterCreateEvent event) {
        ListenableFuture<Boolean> future = attemptGiveQuest(event.getPlayerCharacter(), introQuestId);

        future.addListener(() -> {
            try {
                if(future.get()) {
                    MessageUtil.sendError(event.getPlayer(), "Failed to give the intro quest! This means you're likely stuck! D:");
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getScheduler()::sync);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onNPCRightClick(NPCRightClickEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getClicker())) return;

        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getClicker()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteractBock(PlayerInteractEvent event) {
        if(event.isCancelled()) return;

        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}