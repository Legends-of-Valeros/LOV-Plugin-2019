package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.quests.TraitQuestGiver;

public class QuestsIntegration extends Integration {
    public QuestsIntegration() {
        NPCsController.getInstance().registerTrait("questgiver", TraitQuestGiver.class);
    }
}
