package com.legendsofvaleros.modules.gear;

import com.codingforcookies.doris.orm.ORMField;
import com.codingforcookies.doris.orm.ORMRegistry;
import com.codingforcookies.doris.orm.ORMTable;
import com.google.gson.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.gear.component.ComponentMap;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.PersistMap;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.util.MessageUtil;
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

    private static ORMTable<Gear> gearTable;

    private static Map<String, Gear> gear = new HashMap<>();

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
                            MessageUtil.sendException(GearController.getInstance(), ex, false);
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
                            GearController.getInstance().getLogger().warning("Error thrown when decoding persist data. Offender: " + entry.getKey() + " as " + c);
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

        gearTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), Gear.class);

        // Bite the bullet and load all gear into memory to prevent code complexity. This was getting
        // out of hand.
        GearController.getInstance().getScheduler().executeInSpigotCircle(() -> {
            reload();
        });
    }

    public static void reload() {
        gear.clear();

        gearTable.query().all().forEach((item, i) -> {
            item.model = Model.get(item.getModelId());

            gear.put(item.getID(), item);
        }).onFinished(() -> {
            GearController.ERROR_ITEM = Gear.fromID("perfectly-generic-item");
            GearController.getInstance().getLogger().info("Loaded " + gear.size() + " items.");
        }).execute(false);
    }

    public static Gear getItem(String id) {
        if(!gear.containsKey(id)) return GearController.ERROR_ITEM;
        return gear.get(id);
    }
}