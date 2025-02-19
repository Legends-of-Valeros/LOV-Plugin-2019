package com.legendsofvaleros.modules.zones;

import com.codingforcookies.ambience.Ambience;
import com.codingforcookies.ambience.PlayerAmbience;
import com.codingforcookies.ambience.Sound;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.features.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.features.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.zones.commands.ZoneCommands;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.modules.zones.event.ZoneActivateEvent;
import com.legendsofvaleros.modules.zones.event.ZoneDeactivateEvent;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.modules.zones.integration.PvPIntegration;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

@DependsOn(PvPController.class)
@DependsOn(PlayerMenu.class)
@DependsOn(ChatController.class)
@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@ModuleInfo(name = "Zones", info = "")
@IntegratesWith(module = PvPController.class, integration = PvPIntegration.class)
public class ZonesController extends ZonesAPI {
    private static ZonesController instance;

    public static ZonesController getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ZoneCommands());

        //deactivate all zones that are without players for 5 minutes
        getScheduler().executeInMyCircleTimer(new InternalTask(() -> {
            for (Zone zone : getZones()) {
                if (! zone.isActive) {
                    continue;
                }

                //keep zone for 5 minutes active - this should prevent some memory-leaks
                if (zone.timeWithoutPlayers > 0 && (System.currentTimeMillis() / 1000L) - zone.timeWithoutPlayers >= 300) {
                    zone.setActive(false);

                    getScheduler().executeInSpigotCircle(() -> {
                        Bukkit.getServer().getPluginManager().callEvent(new ZoneDeactivateEvent(zone));
                    });

                    MessageUtil.sendDebug(Bukkit.getConsoleSender(), "Zone de-activated: " + zone.name + " " + zone.subname);
                }
            }
        }), 20L, 30 * 20L);
    }

    /**
     * Updates the zone and fires aa zone leave or enter event
     * @param p
     */
    private void updateZone(Player p) {
        for (Zone zone : getZones()) {
            if (zone.isInZone(p.getLocation())) {
                Zone previousZone = getZone(p);
                if (zone == previousZone) {
                    return;
                }

                if (previousZone != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new ZoneLeaveEvent(p, previousZone));
                }

                Bukkit.getServer().getPluginManager().callEvent(new ZoneEnterEvent(p, zone));
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onZoneEnter(ZoneEnterEvent event) {
        if (! Characters.isPlayerCharacterLoaded(event.getPlayer())) {
            return;
        }

        //display zone warning
        boolean pvp = PvPController.getInstance().isPvPEnabled() && event.getZone().pvp;
        Title title = new Title(event.getZone().name, event.getZone().subname + (pvp ? ChatColor.RED + "(pvp enabled)" : ""));
        title.setTitleColor(org.bukkit.ChatColor.GOLD);
        title.setSubtitleColor(org.bukkit.ChatColor.WHITE);
        TitleUtil.queueTitle(title, event.getPlayer());

        //play zone sound
        PlayerAmbience a = Ambience.get(event.getPlayer());
        a.clear();

        if (event.getZone().ambience != null) {
            for (Sound s : event.getZone().ambience) {
                a.queueSound(s);
            }
        }

        //Handle zone activation
        Zone zone = event.getZone();
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(event.getPlayer());
        zone.timeWithoutPlayers = 0;

        if (! zone.isInZone(playerCharacter)) {
            zone.playersInZone.add(playerCharacter.getUniqueCharacterId());

            if (! zone.isActive) {
                zone.setActive(true);
                Bukkit.getServer().getPluginManager().callEvent(new ZoneActivateEvent(zone));
                MessageUtil.sendDebug(Bukkit.getConsoleSender(), "Zone activated: " + zone.name + " " + zone.subname);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onZoneLeave(ZoneLeaveEvent event) {
        Zone zone = event.getZone();
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(event.getPlayer());

        if (zone.isInZone(playerCharacter)) {
            zone.playersInZone.remove(playerCharacter.getUniqueCharacterId());

            if (zone.playersInZone.isEmpty()) {
                zone.timeWithoutPlayers = System.currentTimeMillis() / 1000L;
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (! Characters.isPlayerCharacterLoaded(p)) {
            return;
        }

        updateZone(p);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoin(PlayerCharacterFinishLoadingEvent e) {
        updateZone(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerQuit(PlayerCharacterLogoutEvent e) {
        Zone zone = getZone(e.getPlayerCharacter());
        if (zone == null) {
            return;
        }

        Bukkit.getServer().getPluginManager().callEvent(new ZoneLeaveEvent(e.getPlayer(), zone));
    }
}