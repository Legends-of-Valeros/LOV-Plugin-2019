package com.legendsofvaleros.modules.zones;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.api.ISkin;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.zones.api.IZone;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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


        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(IZone.class, new TypeAdapter<IZone>() {
                    @Override
                    public void write(JsonWriter write, IZone zone) throws IOException {
                        write.value(zone.getId());
                    }

                    @Override
                    public IZone read(JsonReader read) throws IOException {
                        // If we reference the interface, then the type should be a string, and we return the stored object.
                        // Note: it must be loaded already, else this returns null.
                        return zones.get(read.nextString());
                    }
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
        }).onFailure(Throwable::printStackTrace);
    }


}
