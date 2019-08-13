package com.legendsofvaleros.modules.quests;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterCreateEvent;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.factions.FactionController;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.quests.TraitQuestGiver;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.questsold.ActiveTracker;
import com.legendsofvaleros.modules.questsold.QuestAPI;
import com.legendsofvaleros.modules.questsold.QuestCommands;
import com.legendsofvaleros.modules.questsold.action.QuestActionFactory;
import com.legendsofvaleros.modules.questsold.action.core.*;
import com.legendsofvaleros.modules.questsold.api.IQuest;
import com.legendsofvaleros.modules.questsold.core.BasicQuest;
import com.legendsofvaleros.modules.questsold.core.QuestFactory;
import com.legendsofvaleros.modules.questsold.core.QuestStatus;
import com.legendsofvaleros.modules.questsold.event.QuestCompletedEvent;
import com.legendsofvaleros.modules.questsold.event.QuestObjectivesStartedEvent;
import com.legendsofvaleros.modules.questsold.event.QuestStartedEvent;
import com.legendsofvaleros.modules.questsold.integration.*;
import com.legendsofvaleros.modules.questsold.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.questsold.objective.core.DummyObjective;
import com.legendsofvaleros.modules.questsold.objective.core.InteractBlockObjective;
import com.legendsofvaleros.modules.questsold.objective.core.ReturnObjective;
import com.legendsofvaleros.modules.questsold.objective.core.TalkObjective;
import com.legendsofvaleros.modules.questsold.prerequisite.PrerequisiteFactory;
import com.legendsofvaleros.modules.questsold.prerequisite.core.*;
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
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

@DependsOn(NPCsController.class)
@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@IntegratesWith(module = BankController.class, integration = BankIntegration.class)
@IntegratesWith(module = FactionController.class, integration = FactionIntegration.class)
@IntegratesWith(module = GearController.class, integration = GearIntegration.class)
@IntegratesWith(module = MobsController.class, integration = MobsIntegration.class)
@IntegratesWith(module = RegionController.class, integration = RegionIntegration.class)
@IntegratesWith(module = SkillsController.class, integration = SkillsIntegration.class)
@IntegratesWith(module = ZonesController.class, integration = ZonesIntegration.class)
// TODO: Create subclass for listeners?
@ModuleInfo(name = "Quests", info = "")
public class QuestController extends QuestAPI {
    public static AdvancementAPI NEW_OBJECTIVES;

    private static QuestController instance;

    public static QuestController getInstance() {
        return instance;
    }

    private String introQuestId;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        introQuestId = getConfig().getString("intro-quest", "intro");

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
        QuestActionFactory.registerType("title", ActionTitle.class);

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

    public Promise<Boolean> attemptGiveQuest(PlayerCharacter pc, String questId) {
        return getQuest(questId).then(val -> {
            if (val.isPresent()) {
                IQuest quest = val.get();

                QuestStatus status = getStatus(pc, quest);

                if (status.canAccept()) {
                    quest.onStart(pc);

                    MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' can be accepted!");

                    return true;
                } else
                    MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' cannot be accepted! Status: " + status.name());
            } else {
                MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' doesn't exist!");
            }

            return false;
        });
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
        if (NEW_OBJECTIVES != null)
            NEW_OBJECTIVES.show(event.getPlayer());
    }

    @EventHandler
    public void onCharacterCreated(PlayerCharacterCreateEvent event) {
        attemptGiveQuest(event.getPlayerCharacter(), introQuestId);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onNPCRightClick(NPCRightClickEvent event) {
        if (event.isCancelled()) return;

        if (!Characters.isPlayerCharacterLoaded(event.getClicker())) return;

        callEvent(event, Characters.getPlayerCharacter(event.getClicker()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteractBock(PlayerInteractEvent event) {
        if (event.isCancelled()) return;

        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        if (event.isCancelled()) return;

        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}