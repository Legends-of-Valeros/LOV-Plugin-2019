package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.gear.GearRegistry;
import com.legendsofvaleros.modules.gear.component.core.*;
import com.legendsofvaleros.modules.quests.action.gear.EquipObjective;
import com.legendsofvaleros.modules.quests.action.gear.FetchForNPCObjective;
import com.legendsofvaleros.modules.quests.action.gear.FetchObjective;
import com.legendsofvaleros.modules.quests.action.QuestActionFactory;
import com.legendsofvaleros.modules.quests.objective.gear.ActionChooseItem;
import com.legendsofvaleros.modules.quests.objective.gear.ActionGiveItem;
import com.legendsofvaleros.modules.quests.objective.gear.ActionRemoveItem;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;

public class GearIntegration extends Integration {
    public GearIntegration() {
        QuestObjectiveFactory.registerType("equip", EquipObjective.class);
        QuestObjectiveFactory.registerType("fetch", FetchObjective.class);
        QuestObjectiveFactory.registerType("fetch_for", FetchForNPCObjective.class);

        QuestActionFactory.registerType("item_give", ActionGiveItem.class);
        QuestActionFactory.registerType("item_remove", ActionRemoveItem.class);
        QuestActionFactory.registerType("item_choose", ActionChooseItem.class);
    }
}
