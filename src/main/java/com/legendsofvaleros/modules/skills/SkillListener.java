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
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.skills.event.SkillPreUseEvent;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import com.legendsofvaleros.modules.gear.component.skills.CastTrigger;
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
}
