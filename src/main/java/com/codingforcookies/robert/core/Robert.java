package com.codingforcookies.robert.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * Plugin container class.
 *
 * @author Stumblinbear
 *
 */
public class Robert implements Listener {
	private static Robert robert;
	public static Robert inst() { return robert; }

	private static JavaPlugin plugin;
	public static JavaPlugin plugin() { return plugin; }

	public static void enablePortable(JavaPlugin p) {
		if(robert != null)
			return;

		robert = new Robert();
		plugin = p;

		plugin.getServer().getPluginManager().registerEvents(robert, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogout(PlayerQuitEvent e) {
		RobertStack.clear(e.getPlayer());
	}
}