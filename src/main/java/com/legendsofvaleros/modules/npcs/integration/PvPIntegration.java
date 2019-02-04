package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.pvp.TraitHonorTrader;

public class PvPIntegration extends Integration {
    public PvPIntegration() {
        NPCs.registerTrait("honor-trader", TraitHonorTrader.class);
    }
}
