package com.legendsofvaleros.modules.gear;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.doris.orm.ORMField;
import com.codingforcookies.doris.orm.ORMRegistry;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.core.PlayerInventoryData;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.gear.commands.ItemCommands;
import com.legendsofvaleros.modules.gear.component.core.*;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.gear.integration.BankIntegration;
import com.legendsofvaleros.modules.gear.integration.SkillsIntegration;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.listener.InventoryListener;
import com.legendsofvaleros.modules.gear.listener.ItemListener;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.skills.SkillsController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.sql.ResultSet;
import java.sql.SQLException;

@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@DependsOn(Hotswitch.class)
@IntegratesWith(module = BankController.class, integration = BankIntegration.class)
@IntegratesWith(module = SkillsController.class, integration = SkillsIntegration.class)
@ModuleInfo(name = "Gear", info = "")
public class GearController extends Module {
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

        GearRegistry.registerComponent("lore", LoreComponent.class);
        GearRegistry.registerComponent("bind", SoulbindComponent.class);

        GearRegistry.registerComponent("require", RequireComponent.class);
        GearRegistry.registerComponent("damage", GearPhysicalDamage.Component.class);
        GearRegistry.registerComponent("durability", GearDurability.Component.class);
        GearRegistry.registerComponent("usable", GearUsable.Component.class);
        GearRegistry.registerComponent("use_speed", GearUseSpeed.Component.class);

        GearRegistry.registerComponent("stats", GearStats.Component.class);

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
}