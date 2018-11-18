package com.legendsofvaleros.modules.regions.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.regions.Region;

public class RegionLeaveEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	public Player getPlayer() { return player; }
	
	private Region region;
	public Region getRegion() { return region; }

	public RegionLeaveEvent(Player player, Region region) {
		this.player = player;
		this.region = region;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}