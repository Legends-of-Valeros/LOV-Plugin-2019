package com.legendsofvaleros.modules.mobs.core;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.time.Instant;

public class SpawnEditorGUI extends GUI {
	private static final DecimalFormat DF = new DecimalFormat("#.00");

	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Models.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }
	
	public SpawnEditorGUI(SpawnArea spawn) {
		super("Spawn Editor");

		slot(1, 1, new ItemBuilder(Material.BOOK)
							.setName("Spawn Options")
							.addLore("Will attempt to spawn every " + (spawn.getInterval() / 20))
							.addLore("  seconds, in which it will spawn 1 enemy ")
							.addLore("  " + DF.format(spawn.getChance()) + "% of the time, " + spawn.getCount() + " times.")
							.addLore("")
							.addLore("There is a " + DF.format(Math.pow(spawn.getChance() / 100D, spawn.getCount()) * 100) + "% chance that all")
							.addLore("  enemies will spawn in one interval.")
							.addLore("")
							.addLore(" Interval: " + spawn.getInterval() + " ticks")
							.addLore(" Chance : " + spawn.getChance() + "%")
							.addLore(" Count   : " + spawn.getCount())

							.addLore("")
							.addLore(" Current : " + spawn.getEntities().size())
							.addLore(" Unpops  : " + spawn.getDespawnedEnemies())
							.addLore(" Last    : " + Instant.ofEpochMilli(spawn.getLastInterval()).toString())
							.create(), null);

		slot(8, 2, Models.stack("menu-decline-button").setName("Delete Spawn").create(), (gui, p, event) -> {
			MobsController.getInstance().removeSpawn(spawn);
			gui.close(p);
		});
	}
}