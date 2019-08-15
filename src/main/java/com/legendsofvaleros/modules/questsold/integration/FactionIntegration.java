package com.legendsofvaleros.modules.questsold.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.questsold.action.faction.ActionReputation;

public class FactionIntegration extends Integration {
    public FactionIntegration() {
        QuestActionFactory.registerType("faction_rep", ActionReputation.class);
    }
}
