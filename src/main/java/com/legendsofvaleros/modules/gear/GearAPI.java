package com.legendsofvaleros.modules.gear;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.gear.component.ComponentMap;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.PersistMap;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.field.RangedValue;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GearAPI extends Module {
    public interface RPC {
        Promise<Gear[]> findGear();
    }

    private RPC rpc;

    private static Map<String, Gear> gear = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(this, RPC.class);

        APIController.getInstance().getGsonBuilder()
            .registerTypeAdapter(RangedValue.class, RangedValue.JSON)
            .registerTypeAdapter(ComponentMap.class, (JsonDeserializer<ComponentMap>) (json, typeOfT, context) -> {
                JsonObject obj = json.getAsJsonObject();
                ComponentMap components = new ComponentMap();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    try {
                        Class<? extends GearComponent<?>> comp = GearRegistry.getComponent(entry.getKey());
                        if (comp == null)
                            throw new RuntimeException("Unknown component on item: Offender: " + entry.getKey());
                        components.put(entry.getKey(), context.deserialize(entry.getValue(), comp));
                    } catch (Exception e) {
                        MessageUtil.sendException(GearController.getInstance(), new Exception(e + ". Offender: " + entry.getKey() + " " + entry.getValue().toString()));
                    }
                }
                return components;
            })
            .registerTypeAdapter(PersistMap.class, (JsonDeserializer<PersistMap>) (json, typeOfT, context) -> {
            JsonObject obj = json.getAsJsonObject();
            PersistMap persists = new PersistMap();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                Type c = GearRegistry.getPersist(entry.getKey());
                try {
                    persists.put(entry.getKey(), context.deserialize(entry.getValue(), c));
                } catch (Exception e) {
                    getLogger().warning("Error thrown when decoding persist data. Offender: " + entry.getKey() + " as " + c);
                    e.printStackTrace();
                }
            }
            return persists;
        });
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        try {
            this.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public Promise<Gear[]> loadAll() {
        return rpc.findGear().onSuccess(val -> {
            gear.clear();

            for(Gear g : val)
                gear.put(g.getId(), g);

            GearController.ERROR_ITEM = Gear.fromId("perfectly-generic-item");
            getLogger().info("Loaded " + gear.size() + " items.");
        }).onFailure(Throwable::printStackTrace);
    }

    public Gear getGear(String id) {
        if(!gear.containsKey(id)) return GearController.ERROR_ITEM;
        return gear.get(id);
    }
}
