package com.legendsofvaleros.modules.regions.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

import java.util.HashMap;
import java.util.Map;

public class PlayerAccessibility {
    @SerializedName("_id")
    final PlayerCharacter pc;

    final Map<IRegion, Boolean> regions;

    public PlayerAccessibility(PlayerCharacter pc) {
        this.pc = pc;

        this.regions = new HashMap<>();
    }

    public boolean hasAccess(IRegion region) {
        if(!regions.containsKey(region)) return region.isAllowedByDefault();
        return regions.get(region);
    }

    public void setAccessibility(IRegion region, boolean accessible) {
        if(region.isAllowedByDefault() == accessible) {
            regions.remove(region);
            return;
        }

        regions.put(region, accessible);
    }
}
