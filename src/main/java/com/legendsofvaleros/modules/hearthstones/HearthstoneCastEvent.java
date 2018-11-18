package com.legendsofvaleros.modules.hearthstones;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class HearthstoneCastEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	private boolean cancelled = false;
	@Override public boolean isCancelled() { return cancelled; }
	@Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
	
	public HearthstoneCastEvent(Player player) {
		super(player);
	}
}