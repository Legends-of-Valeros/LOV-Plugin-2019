package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.quests.TraitQuestGiver;

public class QuestsIntegration extends Integration {
    public QuestsIntegration() {
        NPCs.registerTrait("questgiver", TraitQuestGiver.class);
    }
}
