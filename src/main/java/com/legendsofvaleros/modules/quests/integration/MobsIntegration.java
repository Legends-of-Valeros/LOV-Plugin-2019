package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.quests.objective.mobs.KillObjective;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;

public class MobsIntegration extends Integration {
    public MobsIntegration() {
        QuestObjectiveFactory.registerType("kill", KillObjective.class);
    }
}
