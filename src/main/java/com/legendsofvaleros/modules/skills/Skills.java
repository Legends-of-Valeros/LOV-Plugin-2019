package com.legendsofvaleros.modules.skills;

import com.codingforcookies.robert.core.RomanNumeral;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.GearRegistry;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.mobs.Mobs;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.playermenu.options.PlayerOptionsOpenEvent;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.objective.stf.QuestObjectiveFactory;
import com.legendsofvaleros.modules.skills.event.*;
import com.legendsofvaleros.modules.skills.gear.GearCharge;
import com.legendsofvaleros.modules.skills.gear.SkillComponent;
import com.legendsofvaleros.modules.skills.gear.SkillResetComponent;
import com.legendsofvaleros.modules.skills.gui.GUISkillsCore;
import com.legendsofvaleros.modules.skills.gui.recharge.TraitRecharger;
import com.legendsofvaleros.modules.skills.mage.TreeMage;
import com.legendsofvaleros.modules.skills.priest.TreePriest;
import com.legendsofvaleros.modules.skills.quest.SkillBindObjective;
import com.legendsofvaleros.modules.skills.quest.SkillUseObjective;
import com.legendsofvaleros.modules.skills.rogue.TreeRogue;
import com.legendsofvaleros.modules.skills.warrior.TreeWarrior;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map.Entry;

@DependsOn(PlayerMenu.class)
@DependsOn(Quests.class)
@DependsOn(Bank.class)
@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(NPCs.class)
@DependsOn(Mobs.class)
@DependsOn(Hotswitch.class)
@DependsOn(GearController.class)
public class Skills extends ModuleListener {
    public static final SkillTree[] skillTrees = new SkillTree[] {
            new TreeWarrior(),
            new TreeRogue(),
            new TreeMage(),
            new TreePriest()
    };

    private static Skills instance;
    public static Skills getInstance() { return instance; }

    public SkillBarManager hotbarManager;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        hotbarManager = new SkillBarManager();

        if(Modules.isLoaded(GearController.class)) {
            GearRegistry.registerComponent("charge", GearCharge.Component.class);
            GearRegistry.registerComponent("skill", SkillComponent.class);
            GearRegistry.registerComponent("skills_reset", SkillResetComponent.class);
        }else
            Skills.getInstance().getLogger().info("GearController integration not enabled.");

        if(Modules.isLoaded(NPCs.class))
            NPCs.registerTrait("recharger", TraitRecharger.class);
        else
            Skills.getInstance().getLogger().info("NPC integration not enabled.");

        if(Modules.isLoaded(Quests.class)) {
            QuestObjectiveFactory.registerType("skill_bind", SkillBindObjective.class);
            QuestObjectiveFactory.registerType("skill_use", SkillUseObjective.class);
        }else
            Skills.getInstance().getLogger().info("Quest integration not enabled.");


        new HotbarListener();

        new FallDamage();
        new NextAttack();
        new OnProjectile();

        for (SkillTree tree : skillTrees) {
            if (tree == null) continue;
            tree.initSkills();
        }

        Skills.getInstance().registerEvents(new SkillListener());
    }

    @EventHandler
    public void onCharacterOptionsOpen(PlayerOptionsOpenEvent event) {
        event.addSlot(Model.stack("menu-skill-tree-button").setName("Skill Tree").create(), (gui, p, type) -> new GUISkillsCore(Characters.getPlayerCharacter(p)).open(p));
    }

    public static void castSkill(CombatEntity caster, Skill skill, int level) {
        if (caster.isPlayer()) {
            PlayerCharacter pc = Characters.getPlayerCharacter((Player) caster.getLivingEntity());

            Cooldowns cooldowns = pc.getCooldowns();
            if (cooldowns.hasCooldown("skill-" + skill.getId()))
                return;

            if (caster.getStats().getRegeneratingStat(pc.getPlayerClass().getSkillCostType()) < skill.getSkillCost(level))
                return;
        }

        if (!caster.getStatusEffects().canUseSkills()) {
            if (caster.isPlayer())
                MessageUtil.sendError(caster.getLivingEntity(), "You are unable to use skills right now!");
        } else {
            SkillPreUseEvent event = new SkillPreUseEvent(caster, skill, level);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (skill.onSkillUse(caster.getLivingEntity().getWorld(), caster, level))
                    Bukkit.getPluginManager().callEvent(new SkillUsedEvent(caster, skill, level));
            }
        }
    }

    public static int getPointCount(Player p) {
        PlayerCharacter character = Characters.getPlayerCharacter(p);

        int points = character.getExperience().getLevel();

        List<Entry<Skill, Integer>> skills = character.getSkillSet().getCharacterSkills();
        for (Entry<Skill, Integer> skill : skills)
            points -= skill.getKey().getTotalLevelCost(skill.getValue());

        return points;
    }

    public static ItemStack getStackForSkillCooldown(PlayerCharacter pc, Entry<Skill, Integer> skill) {
        if (skill == null || skill.getValue() == 0)
            return Model.EMPTY_SLOT;

        ItemBuilder builder = getItemRepresentation(skill.getKey());

        int stackSize = 1;
        if (pc.getCooldowns().hasCooldown("skill-" + skill.getKey().getId())) {
            stackSize = (int) Math.ceil(pc.getCooldowns().getCooldown("skill-" + skill.getKey().getId()).getRemainingDurationMillis() / 1000);
            if (stackSize >= 60)
                stackSize = 60;
            else
                stackSize += 1;
            builder.setEnchanted(false);
        } else
            builder.setEnchanted(true);

        builder.setAmount(stackSize);

        builder.setName(ChatColor.BOLD + skill.getKey().getUserFriendlyName(skill.getValue()) + " " + RomanNumeral.convertToRoman(skill.getValue()));
        builder.addLore(skill.getKey().getSkillDescription(pc, skill.getValue(), false));
        builder.addLore("", ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Right Click" + ChatColor.DARK_GRAY + " to Remove]");
        builder.setTag("skill", skill.getKey().getId());

        return builder.create();
    }

    public static ItemBuilder getItemRepresentation(Skill skill) {
        return Model.stack("skill-" + skill.getId());
    }
}