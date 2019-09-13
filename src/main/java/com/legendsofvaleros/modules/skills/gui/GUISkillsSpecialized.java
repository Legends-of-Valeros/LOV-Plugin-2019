package com.legendsofvaleros.modules.skills.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.ISlotAction;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.modules.skills.core.SkillTree;
import com.legendsofvaleros.modules.skills.core.SkillTree.SpecializedTree;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

public class GUISkillsSpecialized extends GUI {
	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Models.merge("menu-ui-chest-5", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }
	
	private static final int[] SLOTS = new int[] { 1, 1, 1, 2, 1, 3,
													2, 3,
													3, 3, 3, 2, 3, 1,
																4, 1,
													5, 1, 5, 2, 5, 3,
													6, 3,
													7, 3, 7, 2, 7, 1};
	
	GUISkillsCore parent;
	
	public GUISkillsSpecialized(final PlayerCharacter pc, boolean startLeft, SpecializedTree tree) {
		super(tree.name);
		
		int pointCount = SkillsController.getPointCount(pc.getPlayer());

		type(5);
		
		slot(startLeft ? 0 : 8, 0, Models.stack(startLeft ? "menu-arrow-left-button" : "menu-arrow-right-button")
				.setName("Core Skills")
				.setEnchanted(true)
				.create(), (gui, p, clickType) -> new GUISkillsCore(pc).open(p, Flag.REPLACE));

		slot(4, new ItemBuilder(Material.BOOK)
				.setName(null)
				.addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "Skill Points: " + ChatColor.YELLOW + pointCount)
				.setEnchanted(true)
				.setAmount(pointCount < 0 ? 1 : pointCount)
				.create(), null);
		
		for(int i = 0; i < SLOTS.length; i += 2)
			slot(SLOTS[i], SLOTS[i + 1], Models.stack("menu-skill-connector").create(), null);
		
		boolean previousOwned = (pc.getExperience().getLevel() >= 10);
		for(int i = 0; i < tree.skills.length; i++) {
			Skill skill = Skill.getSkillById(tree.skills[i]);
			int level = pc.getSkillSet().getLevel(tree.skills[i]);
			Entry<ItemStack, ISlotAction> stack = SkillTree.buildStack(pointCount, pc, new SimpleImmutableEntry<>(skill, level), previousOwned, (gui, p, button) -> new GUISkillsSpecialized(pc, startLeft, tree).open(p, Flag.REPLACE));
			slot(SLOTS[i * 4], SLOTS[i * 4 + 1], stack.getKey(), stack.getValue());
			previousOwned = level > 0;
		}
	}
}