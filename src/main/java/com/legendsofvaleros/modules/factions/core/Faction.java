package com.legendsofvaleros.modules.factions.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.factions.FactionController;

public class Faction {
    private String id;
    public String getId() {
        return id;
    }

    protected String name;
    public String getName() {
        return name;
    }

    protected String description;
    public String getDescription() {
        return description;
    }

    protected int maxReputation;
    public int getMaxReputation() {
        return maxReputation;
    }

    public Integer getRep(PlayerCharacter pc) {
        return FactionController.getInstance().getApi().getRep(id, pc);
    }

    public Integer editRep(PlayerCharacter pc, int amount) {
        return FactionController.getInstance().getApi().editRep(id, pc, amount);
    }
}