package com.legendsofvaleros.modules.quests.integration;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.GearRegistry;
import com.legendsofvaleros.modules.gear.component.core.*;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.action.gear.EquipObjective;
import com.legendsofvaleros.modules.quests.action.gear.FetchForNPCObjective;
import com.legendsofvaleros.modules.quests.action.gear.FetchObjective;
import com.legendsofvaleros.modules.quests.action.QuestActionFactory;
import com.legendsofvaleros.modules.quests.objective.gear.ActionChooseItem;
import com.legendsofvaleros.modules.quests.objective.gear.ActionGiveItem;
import com.legendsofvaleros.modules.quests.objective.gear.ActionRemoveItem;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GearIntegration extends Integration implements Listener {
    public GearIntegration() {
        QuestController.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("equip", EquipObjective.class);
        QuestObjectiveFactory.registerType("fetch", FetchObjective.class);
        QuestObjectiveFactory.registerType("fetch_for", FetchForNPCObjective.class);

        QuestActionFactory.registerType("item_give", ActionGiveItem.class);
        QuestActionFactory.registerType("item_remove", ActionRemoveItem.class);
        QuestActionFactory.registerType("item_choose", ActionChooseItem.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickupItem(GearPickupEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorEquip(ArmorEquipEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemEquip(ItemEquipEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemUnEquip(ItemUnEquipEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}
