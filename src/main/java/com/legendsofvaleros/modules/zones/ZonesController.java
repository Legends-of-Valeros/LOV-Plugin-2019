package com.legendsofvaleros.modules.zones;

import com.codingforcookies.ambience.Ambience;
import com.codingforcookies.ambience.PlayerAmbience;
import com.codingforcookies.ambience.Sound;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import com.legendsofvaleros.modules.zones.commands.ZoneCommands;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.integration.PvPIntegration;
import com.legendsofvaleros.modules.zones.listener.ZoneListener;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map.Entry;
import java.util.UUID;

@DependsOn(PvPController.class)
@DependsOn(PlayerMenu.class)
@DependsOn(ChatController.class)
@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@IntegratesWith(module = PvPController.class, integration = PvPIntegration.class)
public class ZonesController extends Module {
    private static ZonesController instance;
    public static ZonesController getInstance() { return instance; }

    private static ZoneManager manager;
    public static ZoneManager getManager() { return manager; }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        manager = new ZoneManager();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ZoneCommands());

        registerEvents(new ZoneListener());
    }

    public void onChat(Player p, BaseComponent[] bc) {
        Zone zone = ZonesController.getManager().getZone(p);
        if (zone == null) {
            MessageUtil.sendError(p, "Unable to send message. You are not in a zone!");
            return;
        }

        Player pl;
        for (Entry<UUID, String> entry : ZonesController.getManager().getPlayerZones()) {
            Zone zz = ZonesController.getManager().getZone(entry.getValue());
            if (zz.channel.equals(zone.channel)) {
                pl = Bukkit.getPlayer(entry.getKey());
                if (ChatController.getInstance().isChannelOn(pl, 'Z'))
                    pl.spigot().sendMessage(bc);
            }
        }
    }
}