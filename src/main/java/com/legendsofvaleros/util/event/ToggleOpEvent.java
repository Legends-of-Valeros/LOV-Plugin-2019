package com.legendsofvaleros.util.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ToggleOpEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	private final boolean isOp;
	public boolean isOp() { return isOp; }
	
	public ToggleOpEvent(Player p, boolean isOp) {
		super(p);
		this.isOp = isOp;
	}
}