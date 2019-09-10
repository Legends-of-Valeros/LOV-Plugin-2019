package com.legendsofvaleros.modules.zones;

import com.codingforcookies.ambience.Sound;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.zones.api.IZone;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.Material;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZonesAPI extends ListenerModule {
    public interface RPC {
        Promise<List<Zone>> findZones();
    }

    private RPC rpc;
    private HashMap<String, Zone> zones = new HashMap<>();

    public Collection<Zone> getZones() {
        return zones.values();
    }

    public Zone getZone(String id) {
        return zones.get(id);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        InterfaceTypeAdapter.register(IZone.class,
                                        obj -> obj.getId(),
                                        id -> zones.get(id));

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(Zone.class, (JsonDeserializer<Zone>) (val, typeOfT, context) -> {
                    Zone zone = new Zone();

                    for(Map.Entry<String, JsonElement> zEntry : val.getAsJsonObject().entrySet()) {
                        switch(zEntry.getKey()) {
                            case "_id":
                                zone.id = zEntry.getValue().getAsString();
                                break;
                            case "name":
                                zone.name = zEntry.getValue().getAsString();
                                break;
                            case "sections":
                                JsonArray ja = zEntry.getValue().getAsJsonArray();

                                zone.sections = new Zone.Section[ja.size()];
                                for(int i = 0; i < ja.size(); i++) {
                                    Zone.Section section = zone.new Section();

                                    for(Map.Entry<String, JsonElement> sEntry : ja.get(i).getAsJsonObject().entrySet()) {
                                        switch(sEntry.getKey()) {
                                            case "name":
                                                section.name = sEntry.getValue().getAsString();
                                                break;
                                            case "material":
                                                section.material = Material.valueOf(sEntry.getValue().getAsString());
                                                break;
                                            case "pvp":
                                                section.pvp = sEntry.getValue().getAsBoolean();
                                                break;
                                            case "ambience":
                                                section.ambience = context.deserialize(sEntry.getValue(), Sound[].class);
                                                break;
                                        }
                                    }

                                    zone.sections[i] = section;
                                }
                                break;
                        }
                    }

                    return zone;
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

    public Promise<List<Zone>> loadAll() {
        return rpc.findZones().onSuccess(val -> {
            zones.clear();

            val.orElse(ImmutableList.of()).stream().forEach(zone ->
                    zones.put(zone.id, zone));

            getLogger().info("Loaded " + zones.size() + " zones.");
        });
    }


}
