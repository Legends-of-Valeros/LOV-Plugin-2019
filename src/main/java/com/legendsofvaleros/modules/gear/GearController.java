package com.legendsofvaleros.modules.gear;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.core.PlayerInventoryData;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.gear.commands.ItemCommands;
import com.legendsofvaleros.modules.gear.component.core.*;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.GearInventoryLoader;
import com.legendsofvaleros.modules.gear.integration.BankIntegration;
import com.legendsofvaleros.modules.gear.integration.SkillsIntegration;
import com.legendsofvaleros.modules.gear.listener.InventoryListener;
import com.legendsofvaleros.modules.gear.listener.ItemListener;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.skills.SkillsController;

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

    private GearAPI api;
    public GearAPI getApi() { return api; }

    public static Gear ERROR_ITEM;

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        this.api = new GearAPI();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ItemCommands());

        registerEvents(new ItemListener());
        registerEvents(new InventoryListener());

        GearRegistry.registerComponent("lore", LoreComponent.class);
        GearRegistry.registerComponent("bind", SoulbindComponent.class);

        GearRegistry.registerComponent("require", RequireComponent.class);
        GearRegistry.registerComponent("damage", GearPhysicalDamage.Component.class);
        GearRegistry.registerComponent("durability", GearDurability.Component.class);
        GearRegistry.registerComponent("usable", GearUsable.Component.class);
        GearRegistry.registerComponent("use_speed", GearUseSpeed.Component.class);

        GearRegistry.registerComponent("stats", GearStats.Component.class);

        PlayerInventoryData.method = new GearInventoryLoader();
    }

    @Override
    public void postLoad() {
        try {
            api.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}