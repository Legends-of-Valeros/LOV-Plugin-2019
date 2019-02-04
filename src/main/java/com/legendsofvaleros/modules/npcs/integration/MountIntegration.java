package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.mount.TraitMount;

public class MountIntegration extends Integration {
    public MountIntegration() {
        NPCsController.registerTrait("stablemaster", TraitMount.class);
    }
}
