package com.legendsofvaleros.modules.mobs.core;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.mobs.SpawnManager;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.time.Instant;

public class SpawnEditorGUI extends GUI {
	private static final DecimalFormat DF = new DecimalFormat("#.00");

	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }
	
	public SpawnEditorGUI(SpawnArea spawn) {
		super("Spawn Editor");

		slot(1, 1, new ItemBuilder(Material.BOOK)
							.setName("Spawn Options")
							.addLore("Will attempt to spawn every " + (spawn.getSpawnInterval() / 20))
							.addLore("  seconds, in which it will spawn 1 enemy ")
							.addLore("  " + DF.format(spawn.getSpawnChance()) + "% of the time, " + spawn.getSpawnCount() + " times.")
							.addLore("")
							.addLore("There is a " + DF.format(Math.pow(spawn.getSpawnChance() / 100D, spawn.getSpawnCount()) * 100) + "% chance that all")
							.addLore("  enemies will spawn in one interval.")
							.addLore("")
							.addLore(" Interval: " + spawn.getSpawnInterval() + " ticks")
							.addLore(" Chance : " + spawn.getSpawnChance() + "%")
							.addLore(" Count   : " + spawn.getSpawnCount())

							.addLore("")
							.addLore(" Current : " + spawn.getEntities().size())
							.addLore(" Unpops  : " + spawn.getDespawnedEnemies())
							.addLore(" Last    : " + Instant.ofEpochMilli(spawn.getLastSpawn()).toString())
							.create(), null);

		slot(8, 2, Model.stack("menu-decline-button").setName("Delete Spawn").create(), (gui, p, event) -> {
			SpawnManager.removeSpawn(spawn);
			gui.close(p);
		});
	}
}