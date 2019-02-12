package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.hearthstones.TraitInnkeeper;

public class HearthstonesIntegration extends Integration {
    public HearthstonesIntegration() {
        NPCsController.getInstance().registerTrait("innkeeper", TraitInnkeeper.class);
    }
}
