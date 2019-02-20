package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.fast_travel.TraitFastTravel;

public class FastTravelIntegration extends Integration {
    public FastTravelIntegration() {
        NPCsController.getInstance().registerTrait("fasttravel", TraitFastTravel.class);
    }
}
