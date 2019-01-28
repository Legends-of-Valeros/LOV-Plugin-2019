package com.legendsofvaleros.modules.graveyard;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.mobs.SpawnManager;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

public class GraveyardEditorGUI extends GUI {
	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }

	public GraveyardEditorGUI(Graveyard yard) {
		super("Graveyard Editor");

		slot(8, 2, Model.stack("menu-decline-button").setName("Delete Graveyard").create(), (gui, p, event) -> {
			GraveyardManager.remove(yard);
			gui.close(p);
		});
	}
}