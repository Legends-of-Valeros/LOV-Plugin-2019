package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.mount.TraitMount;

public class MountIntegration extends Integration {
    public MountIntegration() {
        NPCs.registerTrait("stablemaster", TraitMount.class);
    }
}
