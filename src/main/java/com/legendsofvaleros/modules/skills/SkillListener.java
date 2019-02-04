package com.legendsofvaleros.modules.skills;

import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLevelChangeEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.skills.event.BindSkillEvent;
import com.legendsofvaleros.modules.skills.event.SkillPreUseEvent;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import com.legendsofvaleros.modules.skills.gear.CastTrigger;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class SkillListener implements Listener {
    public SkillListener() {
        if(Modules.isLoaded(GearController.class))
            Skills.getInstance().registerEvents(new GearListener());
    }

    @EventHandler
    public void onCharacterLogin(PlayerCharacterStartLoadingEvent e) {
        int level = e.getPlayerCharacter().getExperience().getLevel();

        SkillTree tree = Skills.skillTrees[e.getPlayerCharacter().getPlayerClass().ordinal()];
        String[] coreSkills = tree.getCoreSkills();
        for(int i = 0; i < coreSkills.length; i++) {
            if((i + 1) * 10 > level)
                break;
            if(e.getPlayerCharacter().getSkillSet().getCharacterSkillLevel(coreSkills[i]) == 0)
                e.getPlayerCharacter().getSkillSet().addCharacterSkill(coreSkills[i]);
        }
    }

    @EventHandler
    public void onCharacterLevelUp(PlayerCharacterLevelChangeEvent e) {
        int i = (int)Math.floor(e.getNewLevel() / 10);

        SkillTree tree = Skills.skillTrees[e.getPlayerCharacter().getPlayerClass().ordinal()];
        if(tree.getCoreSkills().length <= i)
            return;
        if(e.getPlayerCharacter().getSkillSet().getCharacterSkill(tree.getCoreSkills()[i]) != null)
            return;

        e.getPlayerCharacter().getSkillSet().addCharacterSkill(tree.getCoreSkills()[i]);

        Map.Entry<Skill, Integer> s = e.getPlayerCharacter().getSkillSet().getCharacterSkill(tree.getCoreSkills()[i]);
        ItemStack stack = Skills.getStackForSkillCooldown(e.getPlayerCharacter(), s);
        List<String> lore = stack.getItemMeta().getLore();
        lore.add(0, stack.getItemMeta().getDisplayName());
        MessageUtil.sendUpdate(e.getPlayer(), new TextBuilder("You've unlocked a new core skill: ").color(ChatColor.AQUA)
                .append(stack.getItemMeta().getDisplayName()).color(ChatColor.LIGHT_PURPLE)
                .hover(String.join("\n", lore)).create());
    }

    @EventHandler
    public void onSkillUsed(SkillUsedEvent e) {
        if(!e.getCombatEntity().isPlayer()) return;

        PlayerCharacter pc = Characters.getPlayerCharacter((Player)e.getLivingEntity());
        Cooldowns cooldowns = pc.getCooldowns();

        e.getCombatEntity().getStats().editRegeneratingStat(pc.getPlayerClass().getSkillCostType(), -e.getSkill().getSkillCost(e.getLevel()));

        cooldowns.offerCooldown("skill-" + e.getSkill().getId(), Cooldowns.CooldownType.CHARACTER_PLAY_TIME, e.getSkill().getSkillCooldownTime(pc, e.getLevel()));
    }

    /**
     * Only activated if GearController is installed. It typically will be, but keeping things in
     * one place will assist testing.
     */
    private class GearListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onSkillUse(SkillPreUseEvent e) {
            if(!e.getCombatEntity().isPlayer()) return;

            PlayerCharacter pc = Characters.getPlayerCharacter((Player)e.getLivingEntity());

            if(e.getSkill().doesRequireFocus()) {
                Gear.Instance instance = Gear.Instance.fromStack(pc.getPlayer().getInventory().getItem(Hotswitch.HELD_SLOT));
                if(instance != null && Boolean.TRUE.equals(instance.doTest(new CastTrigger(e.getCombatEntity(), e.getSkill(), e.getLevel()))))
                    return;

                MessageUtil.sendError(pc.getPlayer(), "You are unable to focus " + e.getSkill().getStatUsed().getUserFriendlyName() + " for that skill.");
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onSkillUsed(SkillUsedEvent e) {
            if(!e.getCombatEntity().isPlayer()) return;

            PlayerCharacter pc = Characters.getPlayerCharacter((Player)e.getLivingEntity());

            if(pc.getPlayerClass().getSkillCostType() == RegeneratingStat.MANA) {
                Gear.Instance instance = Gear.Instance.fromStack(pc.getPlayer().getInventory().getItem(Hotswitch.HELD_SLOT));
                if(instance != null) {
                    if(instance.doFire(new CastTrigger(e.getCombatEntity(), e.getSkill(), e.getLevel())) == GearTrigger.TriggerEvent.REFRESH_STACK)
                        pc.getPlayer().getInventory().setItem(Hotswitch.HELD_SLOT, instance.toStack());
                }
            }
        }
    }
}
