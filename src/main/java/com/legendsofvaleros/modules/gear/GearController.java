package com.legendsofvaleros.modules.gear;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.doris.orm.ORMField;
import com.codingforcookies.doris.orm.ORMRegistry;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.core.PlayerInventoryData;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.gear.inventory.InventoryListener;
import com.legendsofvaleros.modules.gear.inventory.ItemListener;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.quests.QuestManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.sql.ResultSet;
import java.sql.SQLException;

@DependsOn(NPCs.class)
@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@DependsOn(Hotswitch.class)
public class GearController extends ModuleListener {
    private static GearController instance;
    public static GearController getInstance() { return instance; }

    public static Gear ERROR_ITEM;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ItemCommands());

        registerEvents(new ItemListener());
        registerEvents(new InventoryListener());

        ItemManager.onEnable();

        ORMRegistry.addMutator(Gear.Data.class, new ORMRegistry.SQLMutator<Gear.Data>() {
            @Override
            public void applyToField(ORMField field) {
                field.sqlType = "TEXT";
            }

            @Override
            public Gear.Data fromSQL(ResultSet result, String key) throws SQLException {
                return Gear.Data.loadData(result.getString(key));
            }

            @Override
            public Object toSQL(Gear.Data value) {
                return value.toString();
            }
        });

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