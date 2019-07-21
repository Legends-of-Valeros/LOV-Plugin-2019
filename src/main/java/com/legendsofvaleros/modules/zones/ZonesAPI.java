package com.legendsofvaleros.modules.zones;

import com.google.common.collect.ImmutableList;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.zones.core.MaterialWithData;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ZonesAPI extends ModuleListener {
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

    public Zone getZone(Player p) {
        if (!Characters.isPlayerCharacterLoaded(p)) {
            return null;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(p);
        for (Zone zone : getZones()) {
            if (zone.playersInZone.contains(playerCharacter.getUniqueCharacterId())) {
                return zone;
            }
        }
        MessageUtil.sendInfo(Bukkit.getConsoleSender(), "WARNING - " + p.getDisplayName() + " is not in a zone");

        return null;
    }

    public Zone getZone(PlayerCharacter playerCharacter) {
        if (!playerCharacter.isCurrent()) {
            return null;
        }
        for (Zone zone : getZones()) {
            if (zone.playersInZone.contains(playerCharacter.getUniqueCharacterId())) {
                return zone;
            }
        }
        MessageUtil.sendInfo(Bukkit.getConsoleSender(), "WARNING - " + playerCharacter.getPlayer().getDisplayName() + " is not in a zone");
        return null;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(MaterialWithData.class, new TypeAdapter<MaterialWithData>() {
                    @Override
                    public void write(JsonWriter jsonWriter, MaterialWithData mat) throws IOException {
                        jsonWriter.value(mat.type.name() + (mat.data != null ? ":" + mat.data : ""));
                    }

                    @Override
                    public MaterialWithData read(JsonReader jsonReader) throws IOException {
                        return new MaterialWithData(jsonReader.nextString());
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
