package com.legendsofvaleros.modules.quests;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterCreateEvent;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
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
import com.legendsofvaleros.modules.quests.events.QuestEndedEvent;
import com.legendsofvaleros.modules.quests.events.QuestStartedEvent;
import com.legendsofvaleros.modules.quests.nodes.character.*;
import com.legendsofvaleros.modules.quests.nodes.entity.*;
import com.legendsofvaleros.modules.quests.nodes.gear.*;
import com.legendsofvaleros.modules.quests.nodes.npc.*;
import com.legendsofvaleros.modules.quests.nodes.quest.*;
import com.legendsofvaleros.modules.quests.nodes.utility.*;
import com.legendsofvaleros.modules.quests.nodes.world.*;
import com.legendsofvaleros.modules.skills.event.BindSkillEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import io.chazza.advancementapi.AdvancementAPI;
import io.chazza.advancementapi.FrameType;
import io.chazza.advancementapi.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

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
        getEventRegistry().addHandler(BindSkillEvent.class, (event) -> new Player[] { event.getPlayer() });
        getEventRegistry().addHandler(CombatEngineDamageEvent.class, (event) -> {
            if(event.getAttacker().isPlayer() && event.getDamaged().isPlayer()) {
                return new Player[] { (Player)event.getAttacker(), (Player)event.getDamaged() };
            }else if(event.getAttacker().isPlayer()) {
                return new Player[] { (Player)event.getAttacker() };
            }else if(event.getDamaged().isPlayer()) {
                return new Player[] { (Player)event.getDamaged() };
            }

            return null;
        });
        getEventRegistry().addHandler(CombatEngineDeathEvent.class, (event) -> {
            if(event.getKiller().isPlayer() && event.getDied().isPlayer()) {
                return new Player[] { (Player)event.getKiller(), (Player)event.getDied() };
            }else if(event.getKiller().isPlayer()) {
                return new Player[] { (Player)event.getKiller() };
            }else if(event.getDied().isPlayer()) {
                return new Player[] { (Player)event.getDied() };
            }

            return null;
        });

        getLogger().info("is registering prerequisites");
        getPrerequisiteRegistry().addType("class", ClassPrerequisite.class);
        getPrerequisiteRegistry().addType("race", RacePrerequisite.class);
        getPrerequisiteRegistry().addType("level", LevelPrerequisite.class);
        getPrerequisiteRegistry().addType("quests", QuestsPrerequisite.class);
        // PrerequisiteRegistry.addType("time", TimePrerequisite.class);

        getLogger().info("is registering nodes");
        {
            getLogger().info(" - Quest nodes");
            {
                getNodeRegistry().addType("quest:event_started", QuestStartedNode.class);

                getNodeRegistry().addType("quest:trigger_success", QuestSuccessNode.class);
                getNodeRegistry().addType("quest:trigger_fail", QuestFailNode.class);
                getNodeRegistry().addType("quest:trigger_reset", QuestResetNode.class);

                getNodeRegistry().addType("quest:log", QuestLogNode.class);

                getNodeRegistry().addType("quest:offer", QuestOfferNode.class);
                getNodeRegistry().addType("quest:listener", QuestEventsNode.class);
            }

            getLogger().info(" - Gear nodes");
            {
                getNodeRegistry().addType("gear:reference", ReferenceGearNode.class);

                getNodeRegistry().addType("gear:has", HasItemNode.class);
                getNodeRegistry().addType("gear:give", AddItemNode.class);
                getNodeRegistry().addType("gear:remove", RemoveItemNode.class);

                getNodeRegistry().addType("gear:select", SelectItemNode.class);
                getNodeRegistry().addType("gear:random", RandomItemNode.class);

                getNodeRegistry().addType("gear:fetch", FetchItemNode.class);
                getNodeRegistry().addType("gear:fetch_for", FetchItemForNode.class);
                getNodeRegistry().addType("gear:equip", EquipItemNode.class);
                getNodeRegistry().addType("gear:repair", RepairItemNode.class);
            }

            getLogger().info(" - World nodes");
            {
                getNodeRegistry().addType("world:flag_get", GetWorldFlagNode.class);
                getNodeRegistry().addType("world:flag_set", SetWorldFlagNode.class);
                getNodeRegistry().addType("world:flag_check", CheckWorldFlagNode.class);

                getNodeRegistry().addType("world:region_access", AccessRegionNode.class);
                getNodeRegistry().addType("world:region_deny", DeclineRegionNode.class);
                getNodeRegistry().addType("world:region_enter", EnterRegionNode.class);
                getNodeRegistry().addType("world:region_exit", ExitRegionNode.class);

                getNodeRegistry().addType("world:particle", ParticleNode.class);
                getNodeRegistry().addType("world:sound", SoundNode.class);

                getNodeRegistry().addType("world:interact_block", InteractBlockNode.class);
                getNodeRegistry().addType("world:interact_block_with", InteractBlockWithItemNode.class);

                getNodeRegistry().addType("world:zone_enter", EnterZoneNode.class);
                getNodeRegistry().addType("world:zone_exit", ExitZoneNode.class);
            }

            getLogger().info(" - Entity nodes");
            {
                getNodeRegistry().addType("entity:reference", ReferenceEntityNode.class);

                getNodeRegistry().addType("entity:conquer_region", ConquerRegionNode.class);
                getNodeRegistry().addType("entity:conquer_zone", ConquerZoneNode.class);
                getNodeRegistry().addType("entity:damage", DamageNode.class);
                getNodeRegistry().addType("entity:kill", KillNode.class);
            }

            getLogger().info(" - NPC nodes");
            {
                getNodeRegistry().addType("npc:reference", ReferenceNPCNode.class);

                getNodeRegistry().addType("npc:follow", FollowNode.class);
                getNodeRegistry().addType("npc:speak", SpeakNode.class);

                getNodeRegistry().addType("npc:talk", TalkNode.class);

                getNodeRegistry().addType("npc:dialog", DialogNode.class);
                getNodeRegistry().addType("npc:dialog_option", DialogOptionNode.class);
            }

            getLogger().info(" - Character nodes");
            {
                getNodeRegistry().addType("character:listen", CharacterEventsNode.class);

                getNodeRegistry().addType("character:status_effect", StatusEffectNode.class);
                getNodeRegistry().addType("character:xp", ExperienceNode.class);
                getNodeRegistry().addType("character:teleport", TeleportNode.class);
                getNodeRegistry().addType("character:credits", ShowCreditsNode.class);

                getNodeRegistry().addType("character:skill_bind", BindSkillNode.class);
                getNodeRegistry().addType("character:skill_use", UseSkillNode.class);

                getNodeRegistry().addType("character:currency_modify", GiveCurrencyNode.class);
                getNodeRegistry().addType("character:reputation_modify", FactionRepNode.class);
            }

            getLogger().info(" - Utility nodes");
            {
                getNodeRegistry().addType("utility:timer", TimerNode.class);
                getNodeRegistry().addType("utility:wait", WaitTicksNode.class);
                getNodeRegistry().addType("utility:command", RunCommandNode.class);
                getNodeRegistry().addType("utility:notify", NotificationNode.class);
                getNodeRegistry().addType("utility:title", TitleNode.class);
                getNodeRegistry().addType("utility:message", MessageNode.class);
            }
        }

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

    public boolean offerQuest(IQuest quest, PlayerCharacter pc) {
        QuestStatus status = quest.getStatus(pc);

        // A quest that cannot be accepted cannot be offered.
        if (!status.canAccept()) {
            return false;
        }

        // If it's a forced quest, show the quest dialog and accept it immediately.
        if(quest.isForced()) {
            startQuest(quest, pc);

        // If it's not forced, show the quest dialog.
        }else{

        }

        return true;
    }

    public Boolean startQuest(IQuest quest, PlayerCharacter pc) {
        QuestStatus status = quest.getStatus(pc);

        if (status.canAccept()) {
            IQuestInstance instance = quest.getInstance(pc);

            // Track the instance
            quest.setInstance(pc.getUniqueCharacterId(), instance);
            addPlayerQuest(pc, instance);

            // Activate the instance. This works for inactive and repeatable quests.
            instance.setState(QuestState.ACTIVE);

            Bukkit.getPluginManager().callEvent(new QuestStartedEvent(instance));

            MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + quest.getId() + "' accepted!");

            return true;
        } else
            MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + quest.getId() + "' cannot be accepted! Status: " + status.name());

        return false;
    }

    public void completeQuest(IQuest quest, PlayerCharacter pc) {
        IQuestInstance instance = quest.getInstance(pc);

        instance.setState(QuestState.SUCCESS);

        Bukkit.getPluginManager().callEvent(new QuestEndedEvent(instance));
    }

    public void failQuest(IQuest quest, PlayerCharacter pc) {
        IQuestInstance instance = quest.getInstance(pc);

        instance.setState(QuestState.FAILED);

        Bukkit.getPluginManager().callEvent(new QuestEndedEvent(instance));
    }

    public void resetQuest(IQuest quest, PlayerCharacter pc) {
        abandonQuest(quest, pc);

        this.removeQuestProgress(quest, pc);
    }

    public void abandonQuest(IQuest quest, PlayerCharacter pc) {
        IQuestInstance instance = quest.getInstance(pc);

        instance.setState(QuestState.ABANDONED);

        // If it was active, fore the end quest event.
        if(instance.getState().isActive())
            Bukkit.getPluginManager().callEvent(new QuestEndedEvent(instance));
    }

    @EventHandler
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

    /*@EventHandler
    public void onNewObjectives(QuestObjectivesStartedEvent event) {
        // TODO: Update to new stage event listener or objective updated listener?
        if (NEW_OBJECTIVES != null)
            NEW_OBJECTIVES.show(event.getPlayer());
    }*/

    @EventHandler
    public void onCharacterCreated(PlayerCharacterCreateEvent event) {
        // Give the intro quest to all new characters created
        getQuestBySlug(introQuestId).onSuccess(val -> {
            if(val.isPresent()) {
                startQuest(val.get(), event.getPlayerCharacter());
            }else{
                MessageUtil.sendException(this, event.getPlayer(), new NullPointerException("Intro quest '" + introQuestId + "' not found!"));
            }
        });
    }
}