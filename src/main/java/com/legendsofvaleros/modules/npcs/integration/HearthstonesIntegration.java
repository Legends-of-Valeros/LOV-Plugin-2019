package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.trait.hearthstones.TraitInnkeeper;
import com.legendsofvaleros.modules.npcs.NPCs;

public class HearthstonesIntegration extends Integration {
    public HearthstonesIntegration() {
        NPCs.registerTrait("innkeeper", TraitInnkeeper.class);
    }
}
