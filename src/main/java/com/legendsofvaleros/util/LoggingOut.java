package com.legendsofvaleros.util;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks whether players are currently in the process of logging out (which is not easy to
 * ascertain through Bukkit's API).
 */
public class LoggingOut {

	// prevents accidental construction
	private LoggingOut() {}

	private static Set<UUID> loggingOut;

	/**
	 * Starts this listening to player logouts.
	 */
	public static void onEnable() {
		loggingOut = new HashSet<>();
		Bukkit.getPluginManager().registerEvents(new QuitListener(), LegendsOfValeros.getInstance());
	}

	/**
	 * Gets whether a given player is currently in the process of logging out.
	 * 
	 * @param playerId The name of the player that is logging out.
	 * @return <code>true</code> if the player is currently logging out. <code>false</code> if they
	 *         are not logging out or have already finished logging out.
	 */
	public static boolean isLoggingOut(UUID playerId) {
		return loggingOut.contains(playerId);
	}

	private static class QuitListener implements Listener {

		// tracks players who are in the process of logging off
		@EventHandler(priority = EventPriority.LOWEST)
		public void onPlayerQuitLowest(PlayerQuitEvent event) {
			loggingOut.add(event.getPlayer().getUniqueId());
		}

		// cleans up players who are in the process of logging off
		@EventHandler(priority = EventPriority.MONITOR)
		public void onPlayerQuitMonitor(PlayerQuitEvent event) {
			loggingOut.remove(event.getPlayer().getUniqueId());
		}
	}

}
