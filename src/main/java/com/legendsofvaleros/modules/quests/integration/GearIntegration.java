package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.gear.GearRegistry;
import com.legendsofvaleros.modules.gear.component.*;
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
        GearRegistry.registerComponent("lore", LoreComponent.class);
        GearRegistry.registerComponent("bind", SoulbindComponent.class);

        GearRegistry.registerComponent("require", RequireComponent.class);
        GearRegistry.registerComponent("damage", GearPhysicalDamage.Component.class);
        GearRegistry.registerComponent("durability", GearDurability.Component.class);
        GearRegistry.registerComponent("usable", GearUsable.Component.class);
        GearRegistry.registerComponent("use_speed", GearUseSpeed.Component.class);

        GearRegistry.registerComponent("stats", GearStats.Component.class);

        QuestObjectiveFactory.registerType("equip", EquipObjective.class);
        QuestObjectiveFactory.registerType("fetch", FetchObjective.class);
        QuestObjectiveFactory.registerType("fetch_for", FetchForNPCObjective.class);

        QuestActionFactory.registerType("item_give", ActionGiveItem.class);
        QuestActionFactory.registerType("item_remove", ActionRemoveItem.class);
        QuestActionFactory.registerType("item_choose", ActionChooseItem.class);
    }
}
