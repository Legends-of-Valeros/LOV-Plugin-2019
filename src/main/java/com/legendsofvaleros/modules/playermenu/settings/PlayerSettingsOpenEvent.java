package com.legendsofvaleros.modules.playermenu.settings;

import com.codingforcookies.robert.slot.ISlotAction;
import com.codingforcookies.robert.slot.Slot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerSettingsOpenEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	boolean cancelled = false;
	@Override public boolean isCancelled() { return cancelled; }
	@Override public void setCancelled(boolean cancel) { cancelled = cancel; }
	
	private List<Slot> slots = new ArrayList<>();
	public List<Slot> getSlots() { return slots; }
	public void addSlot(ItemStack stack, ISlotAction action) {
		slots.add(new Slot(stack, action));
	}
	
	public PlayerSettingsOpenEvent(Player player) {
		super(player);
	}
}