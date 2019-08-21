package com.legendsofvaleros.modules.gear;

import com.google.common.collect.ImmutableList;
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
import java.util.List;
import java.util.Map;

public class GearAPI extends Module {
    public interface RPC {
        Promise<List<Gear>> findGear();
    }

    private RPC rpc;

    private static Map<String, Gear> gear = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);
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

    public Promise<List<Gear>> loadAll() {
        return rpc.findGear().onSuccess(val -> {
            gear.clear();

            val.orElse(ImmutableList.of()).stream().forEach(g ->
                    gear.put(g.getId(), g));

            GearController.ERROR_ITEM = Gear.fromId("perfectly-generic-item");
            getLogger().info("Loaded " + gear.size() + " items.");
        });
    }

    public Gear getGear(String id) {
        if (!gear.containsKey(id)) return GearController.ERROR_ITEM;
        return gear.get(id);
    }
}
