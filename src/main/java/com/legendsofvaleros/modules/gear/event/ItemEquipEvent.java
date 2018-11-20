package com.legendsofvaleros.modules.gear.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.gear.item.GearItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class ItemEquipEvent extends PlayerCharacterEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	private boolean cancelled = false;
	@Override public boolean isCancelled() { return cancelled; }
	@Override public void setCancelled(boolean cancel) { cancelled = cancel; }
	
	private GearItem.Instance gear;
	public GearItem.Instance getGear() { return gear; }
	
	private EquipmentSlot slot;
	public EquipmentSlot getSlot() { return slot; }
	
	public ItemEquipEvent(PlayerCharacter pc, GearItem.Instance gear, EquipmentSlot slot) {
		super(pc);
		this.gear = gear;
		this.slot = slot;
	}
}