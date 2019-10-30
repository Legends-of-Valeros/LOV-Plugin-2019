package com.legendsofvaleros.modules.zones;

import com.codingforcookies.ambience.Ambience;
import com.codingforcookies.ambience.PlayerAmbience;
import com.codingforcookies.ambience.Sound;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import com.legendsofvaleros.modules.zones.event.*;
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

import java.util.Optional;

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

    // Store a player's current zone so we don't have to loop through every one to find it, again
    private Cache<Player, Zone.Section> playerZone = CacheBuilder.newBuilder().weakKeys().build();

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ZoneCommands());

        // Deactivate all zones that are without players for 5 minutes
        getScheduler().executeInMyCircleTimer(new InternalTask(() -> {
            for (Zone zone : getZones()) {
                if (!zone.isActive) {
                    continue;
                }

                //keep zone for 5 minutes active - this should prevent some memory-leaks
                if (zone.timeWithoutPlayers > 0 && (System.currentTimeMillis() / 1000L) - zone.timeWithoutPlayers >= 300) {
                    zone.setActive(false);

                    getScheduler().executeInSpigotCircle(() -> {
                        Bukkit.getServer().getPluginManager().callEvent(new ZoneDeactivateEvent(zone));
                    });

                    MessageUtil.sendDebug(Bukkit.getConsoleSender(), "Zone de-activated: " + zone.name);
                }
            }
        }), 20L, 30 * 20L);
    }

    public Zone.Section getZone(Player p) {
        if (!Characters.isPlayerCharacterLoaded(p)) {
            return null;
        }

        return getZone(Characters.getPlayerCharacter(p));
    }

    public Zone.Section getZone(PlayerCharacter playerCharacter) {
        if (!playerCharacter.isCurrent()) {
            return null;
        }

        Zone.Section section = playerZone.getIfPresent(playerCharacter.getPlayer());

        if(section != null)
            return section;

        MessageUtil.sendInfo(Bukkit.getConsoleSender(), "WARNING - " + playerCharacter.getPlayer().getDisplayName() + " is not in a zone");

        return null;
    }

    /**
     * Updates the zone and fires aa zone leave or enter event
     */
    private void updateZone(PlayerCharacter pc) {
        Zone.Section oldSection = playerZone.getIfPresent(pc.getPlayer());
        Zone oldZone = oldSection != null ? oldSection.getZone() : null;

        for (Zone zone : getZones()) {
            Optional<Zone.Section> section = zone.getSection(pc.getLocation());

            if(section.isPresent() && oldSection != section.get()) {
                playerZone.put(pc.getPlayer(), section.get());

                if (zone != oldZone) {
                    if (oldZone != null) {
                        Bukkit.getServer().getPluginManager().callEvent(new ZoneLeaveEvent(pc, oldZone));
                    }
                }

                if(oldSection != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new ZoneSectionLeaveEvent(pc, oldSection));
                }

                if(zone != oldZone) {
                    Bukkit.getServer().getPluginManager().callEvent(new ZoneEnterEvent(pc, zone));
                }

                Bukkit.getServer().getPluginManager().callEvent(new ZoneSectionEnterEvent(pc, section.get()));

                return;
            }
        }

        // We should never reach this point, as bedrock is registered as a zone.
        MessageUtil.sendInfo(Bukkit.getConsoleSender(), "WARNING - " + pc.getPlayer().getDisplayName() + " has is in an area with no zone at all! This is heckin bad!");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onZoneEnter(ZoneEnterEvent event) {
        // Handle zone activation
        Zone zone = event.getZone();
        zone.timeWithoutPlayers = 0;

        zone.playersInZone.add(event.getPlayerCharacter().getUniqueCharacterId());

        if (!zone.isActive) {
            zone.setActive(true);

            Bukkit.getServer().getPluginManager().callEvent(new ZoneActivateEvent(zone));

            MessageUtil.sendDebug(Bukkit.getConsoleSender(), "Zone activated: " + zone.name);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onZoneLeave(ZoneLeaveEvent event) {
        Zone zone = event.getZone();

        zone.playersInZone.remove(event.getPlayerCharacter().getUniqueCharacterId());

        if (zone.playersInZone.size() == 0) {
            zone.timeWithoutPlayers = System.currentTimeMillis() / 1000L;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onZoneSectionEnter(ZoneSectionEnterEvent event) {
        // Display zone warning
        boolean pvp = PvPController.getInstance().isPvPEnabled() && event.getSection().pvp;

        Title title = new Title(event.getZone().name,
                        event.getSection().name
                                + (pvp ? ChatColor.RED + "(pvp enabled)" : ""));
        title.setTitleColor(org.bukkit.ChatColor.GOLD);
        title.setSubtitleColor(org.bukkit.ChatColor.WHITE);
        TitleUtil.queueTitle(title, event.getPlayer());

        // Play area sound
        PlayerAmbience a = Ambience.get(event.getPlayer());
        a.clear();

        if (event.getSection().ambience != null) {
            for (Sound s : event.getSection().ambience) {
                a.queueSound(s);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation()))
            return;

        Player p = event.getPlayer();
        if (!Characters.isPlayerCharacterLoaded(p)) {
            return;
        }

        updateZone(Characters.getPlayerCharacter(p));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoin(PlayerCharacterFinishLoadingEvent e) {
        updateZone(e.getPlayerCharacter());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerQuit(PlayerCharacterLogoutEvent e) {
        Zone.Section section = getZone(e.getPlayerCharacter());
        if (section == null) {
            return;
        }

        Zone zone = section.getZone();

        if (zone.playersInZone.contains(e.getPlayerCharacter().getUniqueCharacterId())) {
            zone.playersInZone.remove(e.getPlayerCharacter().getUniqueCharacterId());

            if (zone.playersInZone.isEmpty()) {
                zone.timeWithoutPlayers = System.currentTimeMillis() / 1000L;
            }
        }
    }
}