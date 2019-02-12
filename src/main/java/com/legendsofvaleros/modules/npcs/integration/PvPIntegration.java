package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.pvp.TraitHonorTrader;

public class PvPIntegration extends Integration {
    public PvPIntegration() {
        NPCsController.getInstance().registerTrait("honor-trader", TraitHonorTrader.class);
    }
}
