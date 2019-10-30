package com.legendsofvaleros.modules.factions.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.factions.FactionController;
import com.legendsofvaleros.modules.factions.api.IFaction;

public class Faction implements IFaction {
    private String id;

    @Override
    public String getId() {
        return id;
    }

    protected String name;

    @Override
    public String getName() {
        return name;
    }

    protected String description;

    @Override
    public String getDescription() {
        return description;
    }

    protected int maxReputation;

    @Override
    public int getMaxReputation() {
        return maxReputation;
    }

    public Integer getReputation(PlayerCharacter pc) {
        return FactionController.getInstance().getReputation(this, pc);
    }

    public Integer editReputation(PlayerCharacter pc, int amount) {
        return FactionController.getInstance().editReputation(this, pc, amount);
    }
}