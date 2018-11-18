package com.legendsofvaleros.modules.zones.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.zones.Zone;

public class ZoneEnterEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	public Player getPlayer() { return player; }
	
	private Zone zone;
	public Zone getZone() { return zone; }

	public ZoneEnterEvent(Player player, Zone zone) {
		this.player = player;
		this.zone = zone;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}