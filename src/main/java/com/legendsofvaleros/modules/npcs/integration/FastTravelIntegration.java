package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.trait.fast_travel.TraitFastTravel;
import com.legendsofvaleros.modules.npcs.NPCsController;

public class FastTravelIntegration extends Integration {
    public FastTravelIntegration() {
        NPCsController.registerTrait("fasttravel", TraitFastTravel.class);
    }
}
