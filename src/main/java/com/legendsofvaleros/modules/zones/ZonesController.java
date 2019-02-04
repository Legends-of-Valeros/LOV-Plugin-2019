package com.legendsofvaleros.modules.zones;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.zones.commands.ZoneCommands;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.modules.zones.integration.PvPIntegration;
import com.legendsofvaleros.modules.zones.listener.ZoneListener;
import com.legendsofvaleros.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@DependsOn(PvPController.class)
@DependsOn(PlayerMenu.class)
@DependsOn(ChatController.class)
@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@ModuleInfo(name = "Zones", info = "")
@IntegratesWith(module = PvPController.class, integration = PvPIntegration.class)
public class ZonesController extends Module {
    private static ZonesController instance;

    public static ZonesController getInstance() {
        return instance;
    }

    private static ZoneManager manager;

    public static ZoneManager getManager() {
        return manager;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        manager = new ZoneManager();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ZoneCommands());

        registerEvents(new ZoneListener());
    }

}