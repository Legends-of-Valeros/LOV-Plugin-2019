package com.legendsofvaleros.modules.regions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.hearthstones.event.HearthstoneCastEvent;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;
import com.legendsofvaleros.modules.regions.commands.RegionCommands;
import com.legendsofvaleros.modules.regions.integration.HearthstonesIntegration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@IntegratesWith(module = HearthstoneController.class, integration = HearthstonesIntegration.class)
public class RegionController extends Module {
    private static RegionController instance;
    public static RegionController getInstance() { return instance; }

    public static boolean REGION_DEBUG = false;

    private static RegionManager regionManager;

    public static RegionManager getManager() {
        return regionManager;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        regionManager = new RegionManager();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new RegionCommands());
    }
}