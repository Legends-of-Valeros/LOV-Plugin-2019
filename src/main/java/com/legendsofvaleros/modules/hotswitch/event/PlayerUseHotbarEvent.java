package com.legendsofvaleros.modules.hotswitch.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerUseHotbarEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	private final int hotbar;
	public int getCurrentHotbar() { return hotbar; }
	
	private final int slot;
	public int getSlot() { return slot; }
	
	private final ItemStack stack;
	public ItemStack getItemStack() { return stack; }
	
	public PlayerUseHotbarEvent(Player player, int hotbar, int slot, ItemStack stack) {
		super(player);
		this.hotbar = hotbar;
		this.slot = slot;
		this.stack = stack;
	}
}