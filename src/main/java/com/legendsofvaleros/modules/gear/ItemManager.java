package com.legendsofvaleros.modules.gear;

import com.codingforcookies.doris.orm.ORMField;
import com.codingforcookies.doris.orm.ORMRegistry;
import com.codingforcookies.doris.orm.ORMTable;
import com.google.gson.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.gear.component.impl.ComponentMap;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.PersistMap;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import com.legendsofvaleros.util.field.RangedValue;
import com.legendsofvaleros.util.item.Model;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ItemManager {
    public static Gson gson;

    private static ORMTable<GearItem> gearTable;

    private static Map<String, GearItem> gear = new HashMap<>();

    public static void onEnable() {
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
                            MessageUtil.sendException(Gear.getInstance(), null, ex, true);
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
                            Gear.getInstance().getLogger().warning("Error thrown when decoding persist data. Offender: " + entry.getKey() + " as " + c);
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

        gearTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), GearItem.class);

        // Bite the bullet and load all gear into memory to prevent code complexity. This was getting
        // out of hand.
        Gear.getInstance().getScheduler().executeInSpigotCircle(() -> {
            reload();

            Gear.ERROR_ITEM = GearItem.fromID("perfectly-generic-item");
            Utilities.getInstance().getLogger().info(Gear.ERROR_ITEM.toString());
        });
    }

    public static void reload() {
        gear.clear();

        gearTable.query().all().forEach((item) -> {
            item.model = Model.get(item.getModelId());

            gear.put(item.getID(), item);
        }).execute(false);
    }

    public static GearItem getItem(String id) {
        if(!gear.containsKey(id)) return Gear.ERROR_ITEM;
        return gear.get(id);
    }
}