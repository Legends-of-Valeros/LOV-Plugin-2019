package com.legendsofvaleros.modules.gear.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.item.GearItem.Instance;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class InventoryFullEvent extends PlayerCharacterEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	private GearItem.Instance instance;
	public GearItem.Instance getItem() { return instance; }

	private boolean cancelled = false;
	@Override public boolean isCancelled() { return cancelled; }
	@Override public void setCancelled(boolean cancel) { cancelled = cancel; }
	
	public InventoryFullEvent(PlayerCharacter who, Instance instance) {
		super(who);
		
		this.instance = instance;
	}
}