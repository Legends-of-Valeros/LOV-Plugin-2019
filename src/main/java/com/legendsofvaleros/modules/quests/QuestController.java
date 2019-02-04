package com.legendsofvaleros.modules.quests;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterCreateEvent;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.factions.FactionController;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.quests.action.core.*;
import com.legendsofvaleros.modules.quests.integration.*;
import com.legendsofvaleros.modules.quests.action.QuestActionFactory;
import com.legendsofvaleros.modules.quests.event.QuestCompletedEvent;
import com.legendsofvaleros.modules.quests.event.QuestObjectivesStartedEvent;
import com.legendsofvaleros.modules.quests.event.QuestStartedEvent;
import com.legendsofvaleros.modules.quests.objective.core.DummyObjective;
import com.legendsofvaleros.modules.quests.objective.core.InteractBlockObjective;
import com.legendsofvaleros.modules.quests.objective.core.ReturnObjective;
import com.legendsofvaleros.modules.quests.objective.core.TalkObjective;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.quests.prerequisite.core.*;
import com.legendsofvaleros.modules.quests.prerequisite.PrerequisiteFactory;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressInteger;
import com.legendsofvaleros.modules.quests.progress.ProgressFactory;
import com.legendsofvaleros.modules.quests.core.BasicQuest;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.core.QuestFactory;
import com.legendsofvaleros.modules.quests.core.QuestStatus;
import com.legendsofvaleros.modules.npcs.trait.quests.TraitQuestGiver;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
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

@DependsOn(NPCsController.class)
@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@IntegratesWith(module = BankController.class, integration = BankIntegration.class)
@IntegratesWith(module = FactionController.class, integration = FactionIntegration.class)
@IntegratesWith(module = MobsController.class, integration = MobsIntegration.class)
@IntegratesWith(module = RegionController.class, integration = RegionIntegration.class)
@IntegratesWith(module = SkillsController.class, integration = SkillsIntegration.class)
@IntegratesWith(module = ZonesController.class, integration = ZonesIntegration.class)
public class QuestController extends ModuleListener {
    public static AdvancementAPI NEW_OBJECTIVES;

    private static QuestController instance;
    public static QuestController getInstance() { return instance; }

    private String introQuestId;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        introQuestId = getConfig().getString("intro-gear", "intro");

        QuestManager.onEnable();
        ActiveTracker.onEnable();

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
                    .description("See gear book for details.")
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
                        quest.onStart(pc);

                        MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' can be accepted!");

                        return;
                    }else
                        MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' cannot be accepted! Status: " + status.name());
                }else{
                    MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' doesn't exist!");
                }
            } catch (Exception e) {
                MessageUtil.sendException(QuestController.getInstance(), pc.getPlayer(), e, true);
            }

            ret.set(false);
        }, QuestController.getInstance().getScheduler()::async);

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
        attemptGiveQuest(event.getPlayerCharacter(), introQuestId);
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