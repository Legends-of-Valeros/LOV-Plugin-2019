package com.legendsofvaleros.modules.mobs;

import com.codingforcookies.doris.orm.ORMField;
import com.codingforcookies.doris.orm.ORMRegistry;
import com.codingforcookies.doris.orm.ORMTable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.mobs.core.Mob;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.inventory.EquipmentSlot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MobManager {
    private static ORMTable<Mob> entitiesTable;

    private static Gson gson;

    public static Cache<String, Mob> entities = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .weakValues()
            .removalListener((entry) -> Mobs.getInstance().getLogger().warning("Entity '" + entry.getKey() + "' removed from the cache."))
            .build();

    public static void clear() {
        entities.invalidateAll();
    }

    public static void onEnable() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Equipment.EquipmentSlot.class, (JsonDeserializer<Equipment.EquipmentSlot>) (json, typeOfT, context) -> {
                    String name = json.getAsString().toUpperCase();

                    if (name.equals("OFFHAND"))
                        name = "OFF_HAND";

                    try {
                        return Equipment.EquipmentSlot.valueOf(name);
                    } catch (Exception e) {
                        try {
                            switch (EquipmentSlot.valueOf(name)) {
                                case HEAD:
                                    return Equipment.EquipmentSlot.HELMET;
                                case CHEST:
                                    return Equipment.EquipmentSlot.CHESTPLATE;
                                case LEGS:
                                    return Equipment.EquipmentSlot.LEGGINGS;
                                case FEET:
                                    return Equipment.EquipmentSlot.BOOTS;
                                default:
                                    return null;
                            }
                        } catch (Exception e1) {
                            throw new RuntimeException("Unknown equipment slot. Offender: " + name);
                        }
                    }
                })
                .create();

        ORMRegistry.addMutator(Mob.StatsMap.class, new ORMRegistry.SQLMutator<Mob.StatsMap>() {
            @Override public void applyToField(ORMField field) {
                field.sqlType = "TEXT";
            }

            @Override public Mob.StatsMap fromSQL(ResultSet result, String key) throws SQLException {
                return new Mob.StatsMap(gson.fromJson(result.getString(key), Mob.StatsMap.StatData[].class));
            }

            @Override public Object toSQL(Mob.StatsMap value) {
                return gson.toJson(value.getData());
            }
        });

        entitiesTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), Mob.class, gson);

        Mobs.getInstance().getScheduler().executeInMyCircleTimer(() -> {
            // This is done so we get almost-live updates on GC'd listeners.
            entities.cleanUp();
        }, 0L, 20L);
    }

    public static Mob getEntity(String id) {
        try {
            return loadEntity(id).get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ListenableFuture<Mob> loadEntity(String id) {
        SettableFuture<Mob> ret = SettableFuture.create();

        Mob cached = entities.getIfPresent(id);
        if (cached != null) {
            ret.set(cached);
        } else {
            entitiesTable.query()
                    .get(id)
                    .forEach((mob) -> {
                        entities.put(id, mob);
                        ret.set(mob);
                    })
                    .onEmpty(() -> ret.set(null))
                    .execute(true);
        }

        return ret;
    }
}