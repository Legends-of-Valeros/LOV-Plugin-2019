package com.legendsofvaleros.modules.npcs.trait.bank.repair;

import com.legendsofvaleros.modules.gear.item.Gear;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class RepairItemEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	private final Gear.Instance gear;
	public Gear.Instance getItem() { return gear; }
	
	public RepairItemEvent(Player who, Gear.Instance gear) {
		super(who);
		this.gear = gear;
	}
}