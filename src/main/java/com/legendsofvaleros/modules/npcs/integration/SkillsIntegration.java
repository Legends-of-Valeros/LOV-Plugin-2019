package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.skills.gui.recharge.TraitRecharger;

public class SkillsIntegration extends Integration {
    public SkillsIntegration() {
        NPCs.registerTrait("recharger", TraitRecharger.class);
    }
}
