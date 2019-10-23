package com.legendsofvaleros.modules.skills.core;

import com.legendsofvaleros.util.RomanNumeral;
import com.legendsofvaleros.features.gui.slot.ISlotAction;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.modules.skills.event.BindSkillEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

public abstract class SkillTree {
    public class SpecializedTree {
        public final String name;
        public final String[] skills;

        public SpecializedTree(String name, String... skills) {
            this.name = name;
            this.skills = skills;
        }

        public boolean hasSkill(PlayerCharacter pc) {
            for (String skill : skills) {
                Entry<Skill, Integer> pair = pc.getSkillSet().get(skill);
                if (pair != null && pair.getValue() > 0)
                    return true;
            }
            return false;
        }
    }

    public abstract void initSkills();

    public abstract String getName();

    public abstract String[] getDescription();

    public abstract String[] getCoreSkills();

    public abstract SpecializedTree[] getSpecializedTrees();

    public static Entry<ItemStack, ISlotAction> buildStack(final int pointCount, final PlayerCharacter pc, final Entry<Skill, Integer> skill, boolean canCollect, ISlotAction onSuccess) {
        final int upgradeCost = skill.getKey().getNextLevelCost(skill.getValue() + 1);
        if (upgradeCost == -1) canCollect = false;

        List<String> desc = new ArrayList<>();
        String[] sdesc = skill.getKey().getSkillDescription(pc, skill.getValue(), true);
        if (sdesc.length > 0)
            desc.addAll(Arrays.asList(sdesc));

        desc.add("");

        boolean blep = false;
        if (skill.getKey().getMaxLevel() == skill.getValue()) {
            canCollect = false;
            desc.add(ChatColor.YELLOW + "" + ChatColor.BOLD + "Already Max Level");
        } else if (upgradeCost != -1) {
            if (!canCollect)
                desc.add(ChatColor.RED + "" + ChatColor.BOLD + "Requirements Unsatisfied");
            else {
                desc.add(ChatColor.YELLOW + (skill.getValue() == 0 ? "Purchase" : "Upgrade") + " Cost: " + ChatColor.WHITE + upgradeCost);

                if (pointCount >= upgradeCost) {
                    blep = true;
                    desc.add("");
                    desc.add(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Right Click" + ChatColor.DARK_GRAY + " to Purchase]");
                } else
                    desc.add(ChatColor.RED + "" + ChatColor.BOLD + "Insufficent Points");
            }
        } else
            desc.add(ChatColor.RED + "" + ChatColor.BOLD + "Upgrade Unavailable");

        if (skill.getValue() > 0) {
            if (!blep) desc.add("");
            desc.add(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Press [1-5]" + ChatColor.DARK_GRAY + " to Equip to Hotbar]");
        }

        ItemStack skillStack = SkillsController.getItemRepresentation(skill.getKey())
                .setAmount(skill.getValue() == 0 ? 1 : skill.getValue())
                .setName(ChatColor.WHITE + "" + ChatColor.BOLD + skill.getKey().getUserFriendlyName(skill.getValue()) + " " + RomanNumeral.convertToRoman(skill.getValue()))
                .addLore(desc.toArray(new String[0]))
                .setEnchanted(skill.getValue() > 0)
                .setTag("skill", skill.getKey().getId())
                .create();

        final boolean canUpgrade = canCollect;
        return new SimpleImmutableEntry<>(skillStack, (gui, p, event) -> {
            if (event.getHotbarButton() >= 0) {
                if (event.getHotbarButton() < Hotswitch.SWITCHER_SLOT) {
                    if (skill.getValue() > 0) {
                        SkillsController.getInstance().updateSkillBar(pc,
                                Hotswitch.getInstance().getCurrentHotbar(p.getUniqueId()) * Hotswitch.SWITCHER_SLOT + event.getHotbarButton(),
                                skill.getKey().getId());
                        p.getInventory().setItem(event.getHotbarButton(), skillStack);

                        Bukkit.getPluginManager().callEvent(new BindSkillEvent(p, Hotswitch.getInstance().getCurrentHotbar(p.getUniqueId()), event.getHotbarButton(), skill.getKey().getId()));
                    }
                }
            } else if (canUpgrade && event.isRightClick()) {
                if (pointCount >= upgradeCost) {
                    pc.getSkillSet().add(skill.getKey().getId());
                    onSuccess.doAction(gui, p, event);
                }
            }
        });
    }
}
