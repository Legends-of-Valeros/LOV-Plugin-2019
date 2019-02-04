package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.quests.action.faction.ActionReputation;
import com.legendsofvaleros.modules.quests.action.QuestActionFactory;

public class FactionIntegration extends Integration {
    public FactionIntegration() {
        QuestActionFactory.registerType("faction_rep", ActionReputation.class);
    }
}
