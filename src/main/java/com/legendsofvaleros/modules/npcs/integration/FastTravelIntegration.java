package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.trait.fast_travel.TraitFastTravel;
import com.legendsofvaleros.modules.npcs.NPCs;

public class FastTravelIntegration extends Integration {
    public FastTravelIntegration() {
        NPCs.registerTrait("fasttravel", TraitFastTravel.class);
    }
}
