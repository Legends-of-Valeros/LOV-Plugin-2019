package com.legendsofvaleros.modules.graveyard.core;

import com.codingforcookies.robert.core.GUI;
import com.legendsofvaleros.modules.graveyard.GraveyardController;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class GraveyardEditorGUI extends GUI {
	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }

	public GraveyardEditorGUI(Graveyard yard) {
		super("Graveyard Editor");

		slot(8, 2, Model.stack("menu-decline-button").setName("Delete Graveyard").create(), (gui, p, event) -> {
			GraveyardController.getInstance().removeGraveyard(yard);
			gui.close(p);
		});
	}
}