package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.skills.gui.recharge.TraitRecharger;

public class SkillsIntegration extends Integration {
    public SkillsIntegration() {
        NPCsController.registerTrait("recharger", TraitRecharger.class);
    }
}
