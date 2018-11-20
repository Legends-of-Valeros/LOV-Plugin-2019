package com.legendsofvaleros.modules.factions.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.factions.Faction;
import org.bukkit.event.HandlerList;

public class FactionReputationChangeEvent extends PlayerCharacterEvent {
	private static final HandlerList handlers = new HandlerList();
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	private final Faction faction;
	public Faction getFaction() { return faction; }
	
	private final int change;
	public int getChange() { return change; }
	
	private final int reputation;
	public int getReputation() { return reputation; }
	
	public FactionReputationChangeEvent(PlayerCharacter pc, Faction faction, int change, int reputation) {
		super(pc);
		this.faction = faction;
		this.change = change;
		this.reputation = reputation;
	}
}