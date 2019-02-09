package com.legendsofvaleros.modules.graveyard;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.util.Collection;

public class GraveyardManager {
    private static final String GRAVEYARD_TABLE = "graveyards";
    private static final String GRAVEYARD_ZONE = "graveyard_zone";
    private static final String GRAVEYARD_WORLD = "graveyard_world";
    private static final String GRAVEYARD_POSITION = "graveyard_position";
    private static final String GRAVEYARD_RADIUS = "graveyard_radius";

    private static TableManager manager;

    private static Multimap<String, Graveyard> graveyards = HashMultimap.create();

    public static void onEnable() {
        manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GRAVEYARD_TABLE);

        manager.primary(GRAVEYARD_ZONE, "VARCHAR(64)")
                .primary(GRAVEYARD_WORLD, "VARCHAR(64)")
                .primary(GRAVEYARD_POSITION, "VARCHAR(64)")
                .column(GRAVEYARD_RADIUS, "SMALLINT UNSIGNED").create();

        manager.query()
                .all()
                .callback((statement, count) -> {
                    ResultSet result = statement.getResultSet();

                    while (result != null && result.next()) {
                        String[] pos = result.getString(GRAVEYARD_POSITION).split(",");

                        Zone z = ZonesController.getManager().getZone(result.getString(GRAVEYARD_ZONE));
                        if (z == null) {
                            MessageUtil.sendException(GraveyardController.getInstance(), "Graveyard in an unknown zone! Offender: " + pos[0] + ", " + pos[1] + ", " + pos[2]);
                            continue;
                        }

                        create(z, Bukkit.getWorld(result.getString(GRAVEYARD_WORLD)),
                                Integer.valueOf(pos[0]),
                                Integer.valueOf(pos[1]),
                                Integer.valueOf(pos[2]),
                                result.getShort(GRAVEYARD_RADIUS));
                    }
                })
                .execute(true);
    }

    public static Graveyard create(Zone zone, World world, int x, int y, int z, int radius) {
        Graveyard data = new Graveyard();
        data.zone = zone.id;
        data.worldName = world.getName();
        data.x = x;
        data.y = y;
        data.z = z;
        data.radius = radius;

        graveyards.put(zone.channel, data);

        // If editing is enabled, generate the hologram right away.
        if(LegendsOfValeros.getMode().allowEditing())
            GraveyardController.getInstance().getScheduler().sync(data::getHologram);

        return data;
    }

    public static Graveyard getNearestGraveyard(Zone zone, Location loc) {
        if (graveyards == null || graveyards.size() == 0
                || zone == null || !graveyards.containsKey(zone.channel))
            return null;

        Collection<Graveyard> yards = graveyards.get(zone.channel);

        Graveyard closest = null;
        double distance = Double.MAX_VALUE;
        for (Graveyard data : yards) {
            if (loc.distance(data.getLocation()) < distance)
                closest = data;
        }

        return closest;
    }

    public static void save(Graveyard graveyard) {
        manager.query()
                .insert()
                .values(GRAVEYARD_ZONE, graveyard.zone,
                        GRAVEYARD_WORLD, graveyard.worldName,
                        GRAVEYARD_POSITION, graveyard.x + "," + graveyard.y + "," + graveyard.z,
                        GRAVEYARD_RADIUS, graveyard.radius)
                .onDuplicateUpdate(GRAVEYARD_RADIUS)
                .build()
                .execute(true);
    }

    public static void remove(final Graveyard graveyard) {
        if (graveyard == null)
            return;

        manager.query()
                .remove()
                .where(GRAVEYARD_ZONE, graveyard.zone,
                        GRAVEYARD_WORLD, graveyard.worldName,
                        GRAVEYARD_POSITION, graveyard.x + "," + graveyard.y + "," + graveyard.z)
                .limit(1)
                .build()
                .execute(true);
    }
}