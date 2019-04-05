package com.legendsofvaleros.modules.regions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;
import com.legendsofvaleros.modules.regions.commands.RegionCommands;
import com.legendsofvaleros.modules.regions.core.RegionSelector;
import com.legendsofvaleros.modules.regions.integration.HearthstonesIntegration;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@IntegratesWith(module = HearthstoneController.class, integration = HearthstonesIntegration.class)
@ModuleInfo(name = "Regions", info = "")
public class RegionController extends RegionsAPI {
    private static RegionController instance;
    public static RegionController getInstance() { return instance; }

    public static boolean REGION_DEBUG = false;

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        registerEvents(new RegionSelector());

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new RegionCommands());
    }
}