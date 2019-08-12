package com.legendsofvaleros.modules.questsold.integration;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.questsold.QuestController;
import com.legendsofvaleros.modules.questsold.action.QuestActionFactory;
import com.legendsofvaleros.modules.questsold.action.gear.ActionChooseItem;
import com.legendsofvaleros.modules.questsold.action.gear.ActionGiveItem;
import com.legendsofvaleros.modules.questsold.action.gear.ActionRemoveItem;
import com.legendsofvaleros.modules.questsold.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.questsold.objective.gear.EquipObjective;
import com.legendsofvaleros.modules.questsold.objective.gear.FetchForNPCObjective;
import com.legendsofvaleros.modules.questsold.objective.gear.FetchObjective;
import com.legendsofvaleros.modules.questsold.objective.gear.InteractBlockWithObjective;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GearIntegration extends Integration implements Listener {
    public GearIntegration() {
        QuestController.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("equip", EquipObjective.class);
        QuestObjectiveFactory.registerType("fetch", FetchObjective.class);
        QuestObjectiveFactory.registerType("fetch_for", FetchForNPCObjective.class);
        QuestObjectiveFactory.registerType("interact_block_with", InteractBlockWithObjective.class);

        QuestActionFactory.registerType("item_give", ActionGiveItem.class);
        QuestActionFactory.registerType("item_remove", ActionRemoveItem.class);
        QuestActionFactory.registerType("item_choose", ActionChooseItem.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickupItem(GearPickupEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorEquip(ArmorEquipEvent event) {
        if(event.isCancelled()) return;
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemEquip(ItemEquipEvent event) {
        if(event.isCancelled()) return;
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemUnEquip(ItemUnEquipEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}
