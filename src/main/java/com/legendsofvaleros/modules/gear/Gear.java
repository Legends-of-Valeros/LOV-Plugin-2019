package com.legendsofvaleros.modules.gear;

import com.legendsofvaleros.LegendsOfValeros;
import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.core.PlayerInventoryData;
import com.legendsofvaleros.modules.gear.component.*;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.gear.inventory.InventoryListener;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.quest.*;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.action.stf.ActionFactory;
import com.legendsofvaleros.modules.quests.objective.stf.ObjectiveFactory;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Gear extends ListenerModule {
    private static Gear instance;
    public static Gear getInstance() { return instance; }

    public static GearItem ERROR_ITEM;

    @Override
    public void onLoad() {
        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ItemCommands());

        Bukkit.getPluginManager().registerEvents(new InventoryListener(), LegendsOfValeros.getInstance());

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

        try {
            ERROR_ITEM = GearItem.fromID("perfectly-generic-item").get(5, TimeUnit.SECONDS);
            Utilities.getInstance().getLogger().info(ERROR_ITEM.toString());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override public void onUnload() {

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