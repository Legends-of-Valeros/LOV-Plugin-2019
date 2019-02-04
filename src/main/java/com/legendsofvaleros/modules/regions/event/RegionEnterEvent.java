package com.legendsofvaleros.modules.regions.event;

import com.legendsofvaleros.modules.regions.core.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegionEnterEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	public Player getPlayer() { return player; }
	
	private Region region;
	public Region getRegion() { return region; }

	public RegionEnterEvent(Player player, Region region) {
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