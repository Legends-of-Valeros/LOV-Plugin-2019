package com.legendsofvaleros.modules.factions.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.factions.api.IFaction;

import java.util.HashMap;
import java.util.Map;

public class PlayerFactionReputation {
    @SerializedName("_id")
    final PlayerCharacter pc;

    final Map<IFaction, Integer> reputation;

    public PlayerFactionReputation(PlayerCharacter pc) {
        this.pc = pc;

        this.reputation = new HashMap<>();
    }

    public Integer getReputation(IFaction faction) {
        return this.reputation.getOrDefault(faction, 0);
    }

    /**
     * Returns the new reputation
     */
    public int editReputation(IFaction faction, int change) {
        int newRep = Math.min(getReputation(faction) + change, faction.getMaxReputation());

        this.reputation.put(faction, newRep);

        return newRep;
    }
}