package com.legendsofvaleros.modules.gear.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.gear.item.Gear;
import org.bukkit.event.HandlerList;

public class GearPickupEvent extends PlayerCharacterEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	private final Gear.Instance instance;
	public Gear.Instance getItem() { return instance; }
	
	public GearPickupEvent(PlayerCharacter who, Gear.Instance instance) {
		super(who);
		
		this.instance = instance;
	}
}