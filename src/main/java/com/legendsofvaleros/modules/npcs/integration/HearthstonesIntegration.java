package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.trait.hearthstones.TraitInnkeeper;
import com.legendsofvaleros.modules.npcs.NPCsController;

public class HearthstonesIntegration extends Integration {
    public HearthstonesIntegration() {
        NPCsController.registerTrait("innkeeper", TraitInnkeeper.class);
    }
}
