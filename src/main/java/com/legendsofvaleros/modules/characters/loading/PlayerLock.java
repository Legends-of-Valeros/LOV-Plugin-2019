package com.legendsofvaleros.modules.characters.loading;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Locks players in place so that they cannot move or interact with the world.
 * <p>
 * May also blacken their screens, teleport them to an enclosed space, or otherwise block their
 * vision.
 */
public class PlayerLock {

	private static final long FREEZE_INTERVAL = 20;
	private static final long TP_DELAY = 2;
	private static final long BLOCK_SCREEN_DELAY = 3;

	private static final Multimap<UUID, PlayerLock> locks = HashMultimap.create();
	private static final Map<UUID, ScreenBlocker> screenBlockers = new HashMap<>();
	private static final Map<UUID, Location> lockLocations = new HashMap<>();

	private static LockingListener listener;
	private static FreezeTask freezeTask;

	public static void onEnable() {
		listener = new LockingListener();
		Characters.getInstance().registerEvents(listener);

		freezeTask = new FreezeTask();
		freezeTask.runTaskTimer(LegendsOfValeros.getInstance(), FREEZE_INTERVAL, FREEZE_INTERVAL);
	}

	/*public static void onDisable() {
		locks.clear();
		screenBlockers.clear();

		HandlerList.unregisterAll(listener);
		freezeTask.cancel();
	}*/

	/**
	 * Gets a lock on a player that holds them in place and prevents them from moving, interacting, or
	 * seeing the world.
	 * 
	 * @param player The player to lock.
	 * @return A lock on the player that can be undone with {@link #release()}.
	 */
	public static PlayerLock lockPlayer(Player player) {
		return new PlayerLock(player);
	}

	/**
	 * Gets whether a player currently has a player-lock on them.
	 * 
	 * @param player The player to check for player-locks.
	 * @return <code>true</code> if the player is locked, else <code>false</code>.
	 */
	public static boolean isPlayerLocked(Player player) {
		if (player == null) {
			return false;
		}
		return locks.containsKey(player.getUniqueId());
	}

	private UUID uid;

	private PlayerLock(final Player player) {
		this.uid = player.getUniqueId();

		// first lock, starts locking behavior for the player
		if (!locks.containsKey(uid)) {
			// teleports after a delay. Delay is necessary because teleporting on login doesnt work.
			lockLocations.put(uid, player.getLocation());

			new BukkitRunnable() {
				@Override
				public void run() {
					player.teleport(lockLocations.get(uid));
				}
			}.runTaskLater(LegendsOfValeros.getInstance(), TP_DELAY);
			
			// delays blocking the screen until after the player has been teleported where they are going.
			// It is location-sensitive.
			screenBlockers.put(uid, ScreenBlocker.blockScreen(player, BLOCK_SCREEN_DELAY));

			hidePlayer(player);

			player.setGameMode(GameMode.SPECTATOR);
			player.setFlying(true);
		}

		locks.put(uid, this);
	}

	/**
	 * Releases this lock on the player, allowing them to play and interact again.
	 * <p>
	 * If the player is/was locked by multiple locks, all locks need to be released before they are
	 * allowed to proceed.
	 * <p>
	 * Cannot be undone. Get a new lock with {@link #lockPlayer(Player)} to re-lock a player.
	 */
	public void release() {
		locks.remove(uid, this);

		// no locks left. releases the player
		if (!locks.containsKey(uid)) {

			ScreenBlocker blocker = screenBlockers.remove(uid);
			if (blocker != null) {
				blocker.unblockScreen();
			}

			Player player = Bukkit.getPlayer(uid);
			if (player != null) {
				showPlayer(player);

				player.setGameMode(Bukkit.getDefaultGameMode());
				player.setFlying(false);
			}
		}
	}

	private static void hidePlayer(Player hide) {
		for (Player hideFrom : Bukkit.getOnlinePlayers()) {
			if (!hide.equals(hideFrom)) {
				hideFrom.hidePlayer(hide);
			}
		}
	}

	private static void showPlayer(Player show) {
		for (Player showTo : Bukkit.getOnlinePlayers()) {
			if (!show.equals(showTo)) {
				showTo.showPlayer(show);
			}
		}
	}

	/**
	 * Listens to and modifies bukkit events to keep players locked in place.
	 */
	private static class LockingListener implements Listener {

		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			locks.removeAll(event.getPlayer().getUniqueId());
			screenBlockers.remove(event.getPlayer().getUniqueId());
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onCombatEngineDamage(CombatEngineDamageEvent event) {
			if (locks.containsKey(event.getDamaged().getUniqueId())
					|| (event.getAttacker() != null && locks.containsKey(event.getAttacker().getUniqueId()))) {
				event.setCancelled(true);
			}
		}

		/**
		 * Generically cancels events for locked players.
		 * 
		 * @param event The event to cancel.
		 * @param player The name of the player involved.
		 */
		public void handle(Event event, UUID player) {
			if (event instanceof Cancellable && locks.containsKey(player)) {
				((Cancellable) event).setCancelled(true);
			}
		}

		// second-to-last called priority, last that is supposed to cancel events
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerInteract(PlayerInteractEvent event) {
			handle(event, event.getPlayer().getUniqueId());
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
			handle(event, event.getPlayer().getUniqueId());
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
			handle(event, event.getPlayer().getUniqueId());
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
			handle(event, event.getPlayer().getUniqueId());
		}

	}

	/**
	 * A task that freezes players by teleporting them back to their original position.
	 * <p>
	 * Doing the same thing by cancelling movement events is highly intensive and not recommended.
	 */
	private static class FreezeTask extends BukkitRunnable {
		@Override
		public void run() {
			Player player;
			Location location;
			for (UUID uid : locks.keys()) {
				player = Bukkit.getPlayer(uid);
				location = lockLocations.get(uid);
				if (player != null && Math.abs(player.getLocation().distance(location)) > .2) {
					player.teleport(location);
				}
			}
		}
	}

}
