package com.legendsofvaleros.modules.factions.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.factions.core.Faction;
import org.bukkit.event.HandlerList;

public class FactionReputationChangeEvent extends PlayerCharacterEvent {
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Faction faction;

    public Faction getFaction() {
        return faction;
    }

    private final int reputation;

    public int getReputation() {
        return reputation;
    }

    private final int change;

    public int getChange() {
        return change;
    }

    public FactionReputationChangeEvent(PlayerCharacter pc, Faction faction, int reputation, int change) {
        super(pc);
        this.faction = faction;
        this.reputation = reputation;
        this.change = change;
    }
}