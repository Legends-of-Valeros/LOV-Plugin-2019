package com.legendsofvaleros.modules.gear;

import com.legendsofvaleros.LegendsOfValeros;
import com.codingforcookies.doris.orm.ORMField;
import com.codingforcookies.doris.orm.ORMRegistry;
import com.codingforcookies.doris.orm.ORMTable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.*;
import com.legendsofvaleros.modules.gear.component.impl.ComponentMap;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.PersistMap;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.util.field.RangedValue;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;

public class ItemManager {
    public static Gson gson;

    private static ORMTable<GearItem> gearTable;

    public static Cache<String, GearItem> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .weakValues()
            .build();

    public static void onEnable(JavaPlugin plugin) {
        gson = new GsonBuilder()
                .registerTypeAdapter(RangedValue.class, RangedValue.JSON)
                .registerTypeAdapter(ComponentMap.class, (JsonDeserializer<ComponentMap>) (json, typeOfT, context) -> {
                    JsonObject obj = json.getAsJsonObject();
                    ComponentMap components = new ComponentMap();
                    for (Entry<String, JsonElement> entry : obj.entrySet()) {
                        try {
                            Class<? extends GearComponent<?>> comp = GearRegistry.getComponent(entry.getKey());
                            if (comp == null)
                                throw new RuntimeException("Unknown component on item: Offender: " + entry.getKey());
                            components.put(entry.getKey(), gson.fromJson(entry.getValue(), comp));
                        } catch (Exception e) {
                            Exception ex = new Exception(e + ". Offender: " + entry.getKey() + " " + entry.getValue().toString());
                            MessageUtil.sendException(LegendsOfValeros.getInstance(), null, ex, true);
                        }
                    }
                    return components;
                }).registerTypeAdapter(PersistMap.class, (JsonDeserializer<PersistMap>) (json, typeOfT, context) -> {
                    JsonObject obj = json.getAsJsonObject();
                    PersistMap persists = new PersistMap();
                    for (Entry<String, JsonElement> entry : obj.entrySet()) {
                        Type c = GearRegistry.getPersist(entry.getKey());
                        try {
                            persists.put(entry.getKey(), gson.fromJson(entry.getValue(), c));
                        } catch (Exception e) {
                            LegendsOfValeros.getInstance().getLogger().warning("Error thrown when decoding persist data. Offender: " + entry.getKey() + " as " + c);
                            e.printStackTrace();
                        }
                    }
                    return persists;
                }).create();

        ORMRegistry.addMutator(ComponentMap.class, new ORMRegistry.SQLMutator<ComponentMap>() {
            @Override public void applyToField(ORMField field) {
                field.sqlType = "TEXT";
            }

            @Override public ComponentMap fromSQL(ResultSet result, String key) throws SQLException {
                return gson.fromJson(result.getString(key), ComponentMap.class);
            }

            @Override public Object toSQL(ComponentMap value) {
                return gson.toJson(value);
            }
        });

        gearTable = ORMTable.bind(plugin.getConfig().getString("dbpools-database"), GearItem.class);
    }

    public static ListenableFuture<GearItem> getItem(String id) {
        SettableFuture<GearItem> ret = SettableFuture.create();

        GearItem cached = cache.getIfPresent(id);
        if (cached != null) {
            ret.set(cached);
        } else {
            gearTable.query()
                    .get(id)
                    .forEach((gear) -> {
                        ListenableFuture<Model> future = Model.get(gear.getModelId());
                        future.addListener(() -> {
                            try {
                                gear.model = future.get();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            cache.put(id, gear);

                            ret.set(gear);
                        }, Utilities.asyncExecutor());
                    })
                    .onEmpty(() -> ret.set(Gear.ERROR_ITEM))
                    .execute(true);
        }

        return ret;
    }
}