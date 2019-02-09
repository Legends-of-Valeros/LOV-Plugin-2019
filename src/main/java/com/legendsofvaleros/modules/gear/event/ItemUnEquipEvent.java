package com.legendsofvaleros.modules.gear.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.gear.item.Gear;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class ItemUnEquipEvent extends PlayerCharacterEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	private Gear.Instance gear;
	public Gear.Instance getGear() { return gear; }

	private EquipmentSlot slot;
	public EquipmentSlot getSlot() { return slot; }

	public ItemUnEquipEvent(PlayerCharacter pc, Gear.Instance gear, EquipmentSlot slot) {
		super(pc);
		this.gear = gear;
		this.slot = slot;
	}
}