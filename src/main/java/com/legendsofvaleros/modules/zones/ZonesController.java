package com.legendsofvaleros.modules.zones;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.zones.commands.ZoneCommands;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.modules.zones.event.ZoneActivateEvent;
import com.legendsofvaleros.modules.zones.event.ZoneDeactivateEvent;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.modules.zones.integration.PvPIntegration;
import com.legendsofvaleros.modules.zones.listener.ZoneListener;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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
        registerEvents(new ZoneListener());

        //deactivate all zones that are without players for 5 minutes
        getInstance().getScheduler().executeInMyCircleTimer(new InternalTask(() -> {
            for (Zone zone : getZones()) {
                if (!zone.isActive) {
                    continue;
                }

                //keep zone for 5 minutes active - this should prevent some memory-leaks
                if (zone.timeWithoutPlayers > 0) {
                    if ((System.currentTimeMillis() / 1000L) - zone.timeWithoutPlayers >= 300) {
                        zone.setActive(false);
                        Bukkit.getServer().getPluginManager().callEvent(new ZoneDeactivateEvent(zone));
                        MessageUtil.sendDebug(Bukkit.getConsoleSender(), "Zone de-activated: " + zone.name + " " + zone.subname);
                    }
                }
            }
        }), 20L, 20L);

        registerEvents(new PlayerListener());
        registerEvents(this);

        getScheduler().executeInMyCircleTimer(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (Characters.isPlayerCharacterLoaded(p)) {
                    try {
                        updateZone(p);
                    } catch (Exception e) {
                        MessageUtil.sendSevereException(this, p, e);
                    }
                }
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onZoneEnter(ZoneEnterEvent event) {
        Zone zone = event.getZone();
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(event.getPlayer());
        zone.timeWithoutPlayers = 0;

        if (!zone.isInZone(playerCharacter)) {
            zone.playersInZone.add(playerCharacter.getUniqueCharacterId());

            if (!zone.isActive) {
                zone.setActive(true);
                Bukkit.getServer().getPluginManager().callEvent(new ZoneActivateEvent(zone));
                MessageUtil.sendDebug(Bukkit.getConsoleSender(), "Zone activated: " + zone.name + " " + zone.subname);
            }
        }
    }

    @EventHandler
    public void onZoneLeave(ZoneLeaveEvent event) {
        Zone zone = event.getZone();
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(event.getPlayer());

        if (zone.isInZone(playerCharacter)) {
            zone.playersInZone.remove(playerCharacter.getUniqueCharacterId());

            if (zone.playersInZone.size() == 0) {
                zone.timeWithoutPlayers = System.currentTimeMillis() / 1000L;
            }
        }
    }

    public void updateZone(Player p) {
        for (Zone zone : getZones()) {
            if (zone.isInZone(p.getLocation())) {
                Zone previousZone = getZone(p);
                if (zone == previousZone)
                    return;

                if (previousZone != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new ZoneLeaveEvent(p, previousZone));
                }
                Bukkit.getServer().getPluginManager().callEvent(new ZoneEnterEvent(p, zone));

                if (previousZone != null) {
                    if (zone.y >= previousZone.y) {
                        return;
                    }
                }
            }
        }
    }

    private class PlayerListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void playerJoin(PlayerCharacterFinishLoadingEvent e) {
            //adding a timeout so we can make sure that the character is loaded
            getScheduler().executeInMyCircleLater(() -> {
                updateZone(e.getPlayer());
            }, 1000);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void playerQuit(PlayerCharacterLogoutEvent e) {
            Zone zone = getZone(e.getPlayerCharacter());
            if (zone == null) {
                return;
            }
            if (zone.playersInZone.contains(e.getPlayerCharacter().getUniqueCharacterId())) {
                zone.playersInZone.remove(e.getPlayerCharacter().getUniqueCharacterId());

                if (zone.playersInZone.size() == 0) {
                    zone.timeWithoutPlayers = System.currentTimeMillis() / 1000L;
                }
            }
        }
    }
}