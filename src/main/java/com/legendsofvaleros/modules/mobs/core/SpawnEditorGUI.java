package com.legendsofvaleros.modules.mobs.core;

import com.codingforcookies.robert.core.GUI;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.modules.mobs.SpawnManager;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class SpawnEditorGUI extends GUI {
	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }
	
	public SpawnEditorGUI(SpawnArea spawn) {
		super("Spawn Editor");
		
		slot(8, 2, Model.stack("menu-decline-button").setName("Delete Spawn").create(), (gui, p, event) -> {
			SpawnManager.removeSpawn(spawn);
			gui.close(p);
		});
	}
}