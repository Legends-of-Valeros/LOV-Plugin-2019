package com.legendsofvaleros.modules.graveyard;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.zones.Zone;
import com.legendsofvaleros.modules.zones.Zones;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.World;

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
                .callback((result) -> {
                    while (result != null && result.next()) {
                        Graveyard sd = new Graveyard();

                        sd.zone = result.getString(GRAVEYARD_ZONE);

                        sd.world = result.getString(GRAVEYARD_WORLD);

                        String[] pos = result.getString(GRAVEYARD_POSITION).split(",");
                        sd.position = new int[]{Integer.valueOf(pos[0]), Integer.valueOf(pos[1]), Integer.valueOf(pos[2])};

                        sd.radius = result.getShort(GRAVEYARD_RADIUS);

                        Zone z = Zones.manager().getZone(sd.zone);
                        if (z == null) {
                            MessageUtil.sendException(Graveyards.getInstance(), "Graveyard in an unknown zone! Offender: " + sd.position[0] + ", " + sd.position[1] + ", " + sd.position[2] + " in " + sd.zone, false);
                            return;
                        }

                        graveyards.put(z.channel, sd);
                    }
                })
                .execute(true);
    }

    public static Graveyard create(Zone zone, World world, int x, int y, int z) {
        Graveyard data = new Graveyard();
        data.zone = zone.id;
        data.world = world.getName();
        data.position = new int[]{x, y, z};

        graveyards.put(zone.channel, data);

        return data;
    }

    public static Graveyard getNearestGraveyard(Zone zone, int x, int z) {
        if (graveyards == null || graveyards.size() == 0 || zone == null)
            return null;

        Collection<Graveyard> yards;
        if (graveyards.containsKey(zone.channel))
            yards = graveyards.get(zone.channel);
        else
            yards = graveyards.values();

        Graveyard closest = null;
        double distance = Double.MAX_VALUE;
        for (Graveyard data : yards) {
            double d = Math.sqrt(Math.pow(x - data.position[0], 2)
                    + Math.pow(z - data.position[2], 2));
            if (d < distance)
                closest = data;
        }

        return closest;
    }

    public static void save(Graveyard graveyard) {
        manager.query()
                .insert()
                .values(GRAVEYARD_ZONE, graveyard.zone,
                        GRAVEYARD_WORLD, graveyard.world,
                        GRAVEYARD_POSITION, graveyard.position[0] + "," + graveyard.position[1] + "," + graveyard.position[2],
                        GRAVEYARD_RADIUS, graveyard.radius)
                .onDuplicateUpdate(GRAVEYARD_RADIUS)
                .build()
                .execute(true);
    }

    public static void remove(Zone zone, final Graveyard graveyard) {
        if (graveyard == null)
            return;

        manager.query()
                .remove()
                .where(GRAVEYARD_ZONE, graveyard.zone,
                        GRAVEYARD_WORLD, graveyard.world,
                        GRAVEYARD_POSITION, graveyard.position[0] + "," + graveyard.position[1])
                .limit(1)
                .build()
                .execute(true);
    }
}