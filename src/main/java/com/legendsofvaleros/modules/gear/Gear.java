package com.legendsofvaleros.modules.gear;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.core.PlayerInventoryData;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.gear.component.*;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.gear.inventory.InventoryListener;
import com.legendsofvaleros.modules.gear.inventory.ItemListener;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.quest.*;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.ActionFactory;
import com.legendsofvaleros.modules.quests.objective.stf.ObjectiveFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@DependsOn(NPCs.class)
@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@DependsOn(Hotswitch.class)
@DependsOn(Quests.class)
public class Gear extends ListenerModule {
    private static Gear instance;
    public static Gear getInstance() { return instance; }

    public static GearItem ERROR_ITEM;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ItemCommands());

        registerEvents(new ItemListener());
        registerEvents(new InventoryListener());

        ItemManager.onEnable();

        GearRegistry.registerComponent("lore", LoreComponent.class);
        GearRegistry.registerComponent("bind", SoulbindComponent.class);

        GearRegistry.registerComponent("require", RequireComponent.class);
        GearRegistry.registerComponent("damage", GearDamage.Component.class);
        GearRegistry.registerComponent("durability", GearDurability.Component.class);
        GearRegistry.registerComponent("usable", GearUsable.Component.class);
        GearRegistry.registerComponent("use_speed", GearUseSpeed.Component.class);

        GearRegistry.registerComponent("stats", GearStats.Component.class);

        ObjectiveFactory.registerType("equip", EquipObjective.class);
        ObjectiveFactory.registerType("fetch", FetchObjective.class);
        ObjectiveFactory.registerType("fetch_for", FetchForNPCObjective.class);

        ActionFactory.registerType("item_give", ActionGiveItem.class);
        ActionFactory.registerType("item_remove", ActionRemoveItem.class);
        ActionFactory.registerType("item_choose", ActionChooseItem.class);

        PlayerInventoryData.method = new GearInventoryLoader();
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