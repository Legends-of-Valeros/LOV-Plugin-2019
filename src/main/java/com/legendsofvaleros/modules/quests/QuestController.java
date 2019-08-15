package com.legendsofvaleros.modules.quests;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterCreateEvent;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.quests.TraitQuestGiver;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestState;
import com.legendsofvaleros.modules.quests.api.QuestStatus;
import com.legendsofvaleros.modules.quests.core.prerequisites.ClassPrerequisite;
import com.legendsofvaleros.modules.quests.core.prerequisites.LevelPrerequisite;
import com.legendsofvaleros.modules.quests.core.prerequisites.QuestsPrerequisite;
import com.legendsofvaleros.modules.quests.core.prerequisites.RacePrerequisite;
import com.legendsofvaleros.modules.quests.nodes.StartedNode;
import com.legendsofvaleros.modules.quests.nodes.TestNode;
import com.legendsofvaleros.util.MessageUtil;
import io.chazza.advancementapi.AdvancementAPI;
import io.chazza.advancementapi.FrameType;
import io.chazza.advancementapi.Trigger;
import net.citizensnpcs.api.event.NPCClickEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEvent;

@DependsOn(NPCsController.class)
@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
/*@IntegratesWith(module = BankController.class, integration = BankIntegration.class)
@IntegratesWith(module = FactionController.class, integration = FactionIntegration.class)
@IntegratesWith(module = GearController.class, integration = GearIntegration.class)
@IntegratesWith(module = MobsController.class, integration = MobsIntegration.class)
@IntegratesWith(module = RegionController.class, integration = RegionIntegration.class)
@IntegratesWith(module = SkillsController.class, integration = SkillsIntegration.class)
@IntegratesWith(module = ZonesController.class, integration = ZonesIntegration.class)*/
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

        //ActiveTracker.onEnable();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new QuestCommands());

        registerEvents(new TraitQuestGiver.Marker());

        // Register some basic event handlers.
        getLogger().info("is registering event handlers");
        getEventRegistry().addHandler(NPCClickEvent.class, (event) -> event.getClicker());
        getEventRegistry().addHandler(PlayerEvent.class, (event) -> event.getPlayer());

        getLogger().info("is registering prerequisites");
        getPrerequisiteRegistry().addType("class", ClassPrerequisite.class);
        getPrerequisiteRegistry().addType("race", RacePrerequisite.class);
        getPrerequisiteRegistry().addType("level", LevelPrerequisite.class);
        getPrerequisiteRegistry().addType("quests", QuestsPrerequisite.class);
        // PrerequisiteRegistry.addType("time", TimePrerequisite.class);

        getLogger().info("is registering nodes");
        getNodeRegistry().addType("event_started", StartedNode.class);
        getNodeRegistry().addType("test", TestNode.class);
        /*NodeRegistry.addType("dummy", DummyObjective.class);
        NodeRegistry.addType("talk", TalkObjective.class);
        NodeRegistry.addType("return", ReturnObjective.class);
        NodeRegistry.addType("interact_block", InteractBlockObjective.class);

        NodeRegistry.addType("conversation", ActionConversation.class);
        NodeRegistry.addType("goto", ActionGoTo.class);

        NodeRegistry.addType("command_run", ActionRunCommand.class);
        NodeRegistry.addType("speech", ActionSpeech.class);
        NodeRegistry.addType("wait", ActionWait.class);

        NodeRegistry.addType("quest_new", ActionNewQuest.class);

        NodeRegistry.addType("notify", ActionNotification.class);
        NodeRegistry.addType("title", ActionTitle.class);

        NodeRegistry.addType("text", ActionText.class);

        NodeRegistry.addType("particle", ActionParticle.class);
        NodeRegistry.addType("sound", ActionSound.class);
        NodeRegistry.addType("xp", ActionExperience.class);
        NodeRegistry.addType("teleport", ActionTeleport.class);
        NodeRegistry.addType("show_credits", ActionShowCredits.class);*/

        // Fill in the top two slots of the crafting area
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

    private Promise<Boolean> attemptAcceptQuest(PlayerCharacter pc, String questId) {
        return getQuest(questId).then(val -> {
            if (val.isPresent()) {
                IQuest quest = val.get();

                QuestStatus status = quest.getStatus(pc);

                if (status.canAccept()) {
                    MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' can be accepted!");

                    IQuestInstance instance = quest.getInstance(pc);

                    quest.setInstance(pc.getUniqueCharacterId(), instance);

                    // Activate the instance. This works for inactive and repeatable quests.
                    instance.setState(QuestState.ACTIVE);

                    return true;
                } else
                    MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' cannot be accepted! Status: " + status.name());
            } else {
                MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + questId + "' doesn't exist!");
            }

            return false;
        });
    }

    /*@EventHandler
    public void onQuestStarted(QuestStartedEvent event) {
        Title title = new Title("New Quest", event.getQuest().getName(), 10, 40, 10);
        title.setTimingsToTicks();
        title.setTitleColor(ChatColor.GOLD);
        TitleUtil.queueTitle(title, event.getPlayer());
    }

    @EventHandler
    public void onQuestComplete(QuestEndedEvent event) {
        event.getPlayer().playSound(event.getPlayer().getLocation(), "misc.questcomplete", 1F, 1F);
    }

    // TODO: add quest failure listener

    @EventHandler
    public void onNewObjectives(QuestObjectivesStartedEvent event) {
        // TODO: Update to new stage event listener or objective updated listener?
        if (NEW_OBJECTIVES != null)
            NEW_OBJECTIVES.show(event.getPlayer());
    }*/

    @EventHandler
    public void onCharacterCreated(PlayerCharacterCreateEvent event) {
        // Give the intro quest to all new characters created
        attemptAcceptQuest(event.getPlayerCharacter(), introQuestId);
    }
}