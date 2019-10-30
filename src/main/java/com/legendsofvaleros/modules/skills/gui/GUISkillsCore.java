package com.legendsofvaleros.modules.skills.gui;

import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.core.GuiFlag;
import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.features.gui.slot.ISlotAction;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.modules.skills.core.SkillTree;
import com.legendsofvaleros.modules.skills.core.SkillTree.SpecializedTree;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

public class GUISkillsCore extends GUI {
	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Models.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }
	
	public GUISkillsCore(PlayerCharacter pc) {
		this(pc, SkillsController.getPointCount(pc.getPlayer()));
	}
	
	private GUISkillsCore(final PlayerCharacter pc, final int pointCount) {
		super("Core Skills");

		final SkillTree tree = SkillsController.skillTrees[pc.getPlayerClass().ordinal()];
		final SpecializedTree[] treeSkills = tree.getSpecializedTrees();

		boolean hasSkill1 = treeSkills[0].hasSkill(pc);
		boolean hasSkill2 = treeSkills[1].hasSkill(pc);
		
		slot(0, Models.stack("menu-arrow-left-button")
				.setName(treeSkills[0].name)
				.addLore("", (pc.getExperience().getLevel() >= 10 ? (hasSkill1 ? ChatColor.YELLOW + "Unlocked" : (hasSkill2 ? ChatColor.RED + "Locked" : ChatColor.GREEN + "Available")) : ChatColor.GRAY + "Unlocked at level 10"))
				.setEnchanted(hasSkill1)
				.create(), (gui, p, clickType) -> new GUISkillsSpecialized(pc, true, treeSkills[0]).open(p, GuiFlag.REPLACE));
		slot(8, Models.stack("menu-arrow-right-button")
				.setName(treeSkills[1].name)
				.addLore("", (pc.getExperience().getLevel() >= 10 ? (hasSkill2 ? ChatColor.YELLOW + "Unlocked" : (hasSkill1 ? ChatColor.RED + "Locked" : ChatColor.GREEN + "Available")) : ChatColor.GRAY + "Unlocked at level 10"))
				.setEnchanted(hasSkill2)
				.create(), (gui, p, clickType) -> new GUISkillsSpecialized(pc, false, treeSkills[1]).open(p, GuiFlag.REPLACE));

		slot(4, new ItemBuilder(Material.BOOK)
				.setName(tree.getName())
				.addLore(tree.getDescription())
				.addLore("", ChatColor.GOLD + "" + ChatColor.BOLD + "Skill Points: " + ChatColor.YELLOW + pointCount)
				.setEnchanted(true)
				.setAmount(pointCount < 0 ? 1 : pointCount)
				.create(), null);
		
		int i = 0;

		for(String skillID : tree.getCoreSkills()) {
			Skill skill = Skill.getSkillById(skillID);
			if(skill == null) {
				MessageUtil.sendError(pc.getPlayer(), "Unknown skill. Offender: " + skillID);
				continue;
			}
			
			int level = pc.getSkillSet().getLevel(skillID);
			Entry<ItemStack, ISlotAction> stack = SkillTree.buildStack(pointCount, pc, new SimpleImmutableEntry<>(skill, level), true, (gui, p, button) -> new GUISkillsCore(pc).open(p, GuiFlag.REPLACE));
			slot(1 + i, 1, stack.getKey(), stack.getValue());
			i++;
		}

		if(pc.getPlayer().hasPermission("lov.skills.admin"))
			slot(4, 2, new ItemBuilder(Material.NETHER_STAR).setName("Admin Skills").create(), (gui, p, button) -> {
				new GUIAdminSkills(pc).open(p);
			});
	}
}
