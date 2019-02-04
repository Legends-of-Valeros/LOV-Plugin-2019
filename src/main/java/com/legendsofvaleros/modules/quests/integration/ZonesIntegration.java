package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.quests.objective.zones.EnterZoneObjective;
import com.legendsofvaleros.modules.quests.objective.zones.ExitZoneObjective;

public class ZonesIntegration extends Integration {
    public ZonesIntegration() {
        QuestObjectiveFactory.registerType("zone_enter", EnterZoneObjective.class);
        QuestObjectiveFactory.registerType("zone_exit", ExitZoneObjective.class);
    }
}
