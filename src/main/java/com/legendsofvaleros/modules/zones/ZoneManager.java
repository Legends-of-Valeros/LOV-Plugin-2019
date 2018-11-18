package com.legendsofvaleros.modules.zones;

import com.codingforcookies.ambience.Sound;
import com.codingforcookies.doris.sql.TableManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class ZoneManager implements Listener {
    private static final String ZONE_TABLE = "zones";
    private static final String ZONE_ID = "zone_id";
    private static final String ZONE_Y = "zone_y";
    private static final String ZONE_CHANNEL = "zone_channel";
    private static final String ZONE_NAME = "zone_name";
    private static final String ZONE_SUBNAME = "zone_subname";
    private static final String ZONE_MATERIAL = "zone_material";
    private static final String ZONE_PVP = "zone_pvp";
    private static final String ZONE_AMBIENCE = "zone_ambience";

    private final Gson gson;

    private final TableManager manager;

    private HashMap<String, Zone> zones = new HashMap<>();

    public Zone getZone(String id) {
        return zones.get(id);
    }

    public Collection<Zone> getZones() {
        return zones.values();
    }

    private HashMap<UUID, String> currentZone = new HashMap<>();

    public Set<Entry<UUID, String>> getPlayerZones() {
        return currentZone.entrySet();
    }

    public Zone getZone(Player p) {
        return zones.get(currentZone.get(p.getUniqueId()));
    }

    public ZoneManager() {
        gson = new GsonBuilder().create();

        manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), ZONE_TABLE);

        manager.primary(ZONE_ID, "VARCHAR(24)")
                .column(ZONE_Y, "INT(4)")
                .column(ZONE_CHANNEL, "VARCHAR(16)")
                .column(ZONE_NAME, "VARCHAR(24)")
                .column(ZONE_SUBNAME, "VARCHAR(32)")
                .column(ZONE_MATERIAL, "VARCHAR(64)")
                .column(ZONE_PVP, "BOOLEAN")
                .column(ZONE_AMBIENCE, "TEXT").create();

        loadZones();

        Bukkit.getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());

        Bukkit.getScheduler().runTaskTimerAsynchronously(LegendsOfValeros.getInstance(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!Characters.isPlayerCharacterLoaded(p)) continue;
                try {
                    updateZone(p);
                } catch (Exception e) {
                    MessageUtil.sendException(LegendsOfValeros.getInstance(), p, e, true);
                }
            }
        }, 20L, 20L);
    }

    public void loadZones() {
        manager.query()
                .select()
                .build()
                .callback((result) -> {
                    while (result.next()) {
                        Zone zone = new Zone();
                        zone.id = result.getString(ZONE_ID);
                        zone.y = result.getInt(ZONE_Y);
                        zone.channel = result.getString(ZONE_CHANNEL);
                        zone.name = result.getString(ZONE_NAME);
                        zone.subname = result.getString(ZONE_SUBNAME);

                        String[] material = result.getString(ZONE_MATERIAL).split(":");
                        zone.material = Material.valueOf(material[0]);
                        if (material.length > 1)
                            zone.materialData = Byte.parseByte(material[1]);

                        zone.pvp = result.getBoolean(ZONE_PVP);

                        zone.ambience = gson.fromJson(result.getString(ZONE_AMBIENCE), Sound[].class);

                        zones.put(zone.id, zone);
                    }
                })
                .execute(true);
    }

    /**
     * Returns a Zone if and only if the screen should be updated with the new zone's information.
     */
    public Zone updateZone(Player p) {
        for (Zone zone : zones.values())
            if (zone.isInZone(p.getLocation())) {
                Zone previousZone = getZone(p);
                if (zone == previousZone)
                    return null;

                currentZone.put(p.getUniqueId(), zone.id);

                if (previousZone != null)
                    Bukkit.getServer().getPluginManager().callEvent(new ZoneLeaveEvent(p, previousZone));
                Bukkit.getServer().getPluginManager().callEvent(new ZoneEnterEvent(p, zone));

                if (previousZone != null) {
                    if (zone.y >= previousZone.y)
                        return zone;
                }
                return null;
            }
        return null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoin(PlayerCharacterFinishLoadingEvent e) {
        updateZone(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerQuit(PlayerCharacterLogoutEvent e) {
        currentZone.remove(e.getPlayer().getUniqueId());
    }
}