package com.legendsofvaleros.modules.graveyard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.GearRegistry;
import com.legendsofvaleros.modules.gear.component.ComponentMap;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.PersistMap;
import com.legendsofvaleros.modules.graveyard.core.Graveyard;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class GraveyardAPI {
    public interface RPC {
        Promise<Graveyard[]> findGraveyards();
        Promise<Boolean> saveGraveyard(Graveyard yard);
        Promise<Boolean> deleteGraveyard(Graveyard yard);
    }

    private final RPC rpc;

    private Multimap<String, Graveyard> graveyards = HashMultimap.create();

    public GraveyardAPI() {
        this.rpc = APIController.create(GraveyardController.getInstance(), RPC.class);

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
                    GearController.getInstance().getLogger().warning("Error thrown when decoding persist data. Offender: " + entry.getKey() + " as " + c);
                    e.printStackTrace();
                }
            }
            return persists;
        });
    }

    public Promise<Graveyard[]> loadAll() {
        return rpc.findGraveyards().onSuccess(val -> {
            graveyards.clear();

            for(Graveyard g : val)
                graveyards.put(g.zone.channel, g);

            GraveyardController.getInstance().getLogger().info("Loaded " + graveyards.size() + " graveyards.");
        }).onFailure(Throwable::printStackTrace);
    }

    public Graveyard getNearestGraveyard(Zone zone, Location loc) {
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

    public Promise<Boolean> addGraveyard(Graveyard yard) {
        graveyards.put(yard.zone.channel, yard);

        // If editing is enabled, generate the hologram right away.
        if(LegendsOfValeros.getMode().allowEditing())
            GraveyardController.getInstance().getScheduler().sync(yard::getHologram);

        return rpc.saveGraveyard(yard);
    }

    public Promise<Boolean> removeGraveyard(Graveyard yard) {
        graveyards.remove(yard.zone.channel, yard);

        return rpc.deleteGraveyard(yard);
    }
}
