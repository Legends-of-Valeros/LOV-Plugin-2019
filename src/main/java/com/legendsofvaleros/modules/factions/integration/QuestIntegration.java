package com.legendsofvaleros.modules.factions.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.factions.quest.ActionReputation;
import com.legendsofvaleros.modules.quests.action.stf.QuestActionFactory;

public class QuestIntegration extends Integration {
    QuestIntegration() {
        QuestActionFactory.registerType("faction_rep", ActionReputation.class);
    }
}
