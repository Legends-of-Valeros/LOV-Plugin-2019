package com.legendsofvaleros.modules.hearthstones.core;

import com.codingforcookies.robert.core.StringUtil;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;
import com.legendsofvaleros.modules.hearthstones.event.HearthstoneCastEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles teleporting players to their homes and making sure that teleportation is not exploited.
 */
public class HomeTeleporter implements Listener {

	private final Map<UUID, BukkitRunnable> pendingSpawns;
	private final Map<Player, BlockVector> mustStay;
	private MovementCheckTask movementTask;

	private long warmup;

	public HomeTeleporter(long teleportWarmupSeconds) {
		this.warmup = teleportWarmupSeconds;

		pendingSpawns = new HashMap<>();
		mustStay = new HashMap<>();

		movementTask = new MovementCheckTask();
		movementTask.runTaskTimer(LegendsOfValeros.getInstance(), 40, 5);

		HearthstoneController.getInstance().registerEvents(this);
	}

	/**
	 * Attempts teleportation to a player's home on their behalf.
	 * 
	 * @param pc The player to attempt a teleport for.
	 */
	public void attemptTeleport(final PlayerCharacter pc) {
		HearthstoneCastEvent event = new HearthstoneCastEvent(pc.getPlayer());
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			MessageUtil.sendError(pc.getPlayer(), "You cannot cast your Hearthstone right now.");
			return;
		}
		
		final HomePoint home = HearthstoneController.getInstance().getApi().getHome(pc);
		if (home == null) {
			MessageUtil.sendError(pc.getPlayer(), "You do not have a home set! "
					+ "Right-click an inn keeper to set a home.");
			return;
		}

		if (pendingSpawns.containsKey(pc.getPlayer().getUniqueId())) {
			MessageUtil.sendError(pc.getPlayer(), "You are already casting Hearthstone!");
			return;
		}

		long cooldown = HearthstoneController.getInstance().getApi().getCooldown(pc);
		long timeRemaining = cooldown - System.currentTimeMillis();
		if (timeRemaining > 0) {
			MessageUtil.sendError(pc.getPlayer(), "You cannot cast Hearthstone for another " + StringUtil.getTimeFromMilliseconds(timeRemaining, 2, false) + ".");
			return;
		}

		MessageUtil.sendInfo(pc.getPlayer(), "Casting Hearthstone...");
		MessageUtil.sendInfo(pc.getPlayer(), "You will be teleported to your set home in " + warmup + " seconds. Don't move.");
		mustStay.put(pc.getPlayer(), pc.getPlayer().getLocation().toVector().toBlockVector());
		BukkitRunnable task = new BukkitRunnable() {
			@Override
			public void run() {
				MessageUtil.sendUpdate(pc.getPlayer(), "Returning home...");
				pc.getPlayer().teleport(home.getLocation());
				pc.getPlayer().playSound(pc.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1f, 1f);
				pendingSpawns.remove(pc.getPlayer().getUniqueId());
				mustStay.remove(pc.getPlayer());
				HearthstoneController.getInstance().getApi().addCooldown(pc);
			}
		};
		pendingSpawns.put(pc.getPlayer().getUniqueId(), task);
		task.runTaskLater(LegendsOfValeros.getInstance(), warmup * 20);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player player = (Player) event.getEntity();
			if (pendingSpawns.containsKey(player.getUniqueId())) {
				cancelSpawn(player);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		mustStay.remove(event.getPlayer());
		BukkitRunnable task = pendingSpawns.remove(event.getPlayer().getUniqueId());
		if (task != null) {
			task.cancel();
		}
	}

	private void cancelSpawn(Player player) {
		MessageUtil.sendError(player, "Hearthstone cancelled.");
		mustStay.remove(player);
		BukkitRunnable task = pendingSpawns.remove(player.getUniqueId());
		if (task != null) {
			task.cancel();
		}
	}

	/**
	 * Periodically checks if players waiting to spawn have moved.
	 */
	private class MovementCheckTask extends BukkitRunnable {

		@Override
		public void run() {
			for (Map.Entry<Player, BlockVector> ent : mustStay.entrySet()) {
				BlockVector pVec = ent.getKey().getLocation().toVector().toBlockVector();
				if (!ent.getValue().equals(pVec)) {
					cancelSpawn(ent.getKey());
				}
			}
		}
	}

}
