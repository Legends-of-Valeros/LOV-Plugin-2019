package com.legendsofvaleros.modules.skills.gui;

import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.codingforcookies.robert.window.ExpandingGUI;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.skills.SkillsController;
import com.legendsofvaleros.modules.skills.core.admin.AdminSkills;
import com.legendsofvaleros.modules.skills.event.BindSkillEvent;
import com.legendsofvaleros.util.model.Model;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUIAdminSkills extends ExpandingGUI {
	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }

	public GUIAdminSkills(PlayerCharacter pc) {
		super("Admin Skills");

		List<Slot> slots = new ArrayList<>();

		for(Skill skill : AdminSkills.getSkills()) {
			ItemStack skillStack = new ItemBuilder(Material.NETHER_STAR)
					.setName(skill.getUserFriendlyName(0))
					.addLore(skill.getSkillDescription(pc, 0, false))
					.create();

			slots.add(new Slot(skillStack, (gui, p, event) -> {
				if (event.getHotbarButton() >= 0) {
					if (event.getHotbarButton() < Hotswitch.SWITCHER_SLOT) {
						SkillsController.getInstance().updateSkillBar(pc,
								Hotswitch.getInstance().getCurrentHotbar(p.getUniqueId()) * Hotswitch.SWITCHER_SLOT + event.getHotbarButton(),
								skill.getId());
						p.getInventory().setItem(event.getHotbarButton(), skillStack);

						Bukkit.getPluginManager().callEvent(new BindSkillEvent(p, Hotswitch.getInstance().getCurrentHotbar(p.getUniqueId()), event.getHotbarButton(), skill));
					}
				}
			}));
		}

		initSlots(slots);
	}
}