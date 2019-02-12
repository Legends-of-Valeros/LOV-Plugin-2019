package com.legendsofvaleros.modules.zones;

import com.google.common.collect.ImmutableList;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.zones.core.MaterialWithData;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.util.*;

public class ZonesAPI extends Module {
    public interface RPC {
        Promise<List<Zone>> findZones();
    }

    private RPC rpc;

    private HashMap<String, Zone> zones = new HashMap<>();
    public Zone getZone(String id) {
        return zones.get(id);
    }
    public Collection<Zone> getZones() {
        return zones.values();
    }

    private HashMap<UUID, String> currentZone = new HashMap<>();
    public Set<Map.Entry<UUID, String>> getPlayerZones() {
        return currentZone.entrySet();
    }
    public Zone getZone(Player p) {
        return zones.get(currentZone.get(p.getUniqueId()));
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

        registerEvents(new PlayerListener());

        getScheduler().executeInMyCircleTimer(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!Characters.isPlayerCharacterLoaded(p)) continue;
                try {
                    updateZone(p);
                } catch (Exception e) {
                    MessageUtil.sendSevereException(this, p, e);
                }
            }
        }, 20L, 20L);
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

    /**
     * Returns a Zone if and only if the screen should be updated with the new zone's information.
     */
    public Zone updateZone(Player p) {
        for (Zone zone : zones.values())
            if (zone.isInZone(p.getLocation())) {
                Zone previousZone = getZone(p);
                if (zone == previousZone)
                    return null;

                currentZone.put(p.getUniqueId(), zone.id);

                if (previousZone != null)
                    Bukkit.getServer().getPluginManager().callEvent(new ZoneLeaveEvent(p, previousZone));
                Bukkit.getServer().getPluginManager().callEvent(new ZoneEnterEvent(p, zone));

                if (previousZone != null) {
                    if (zone.y >= previousZone.y)
                        return zone;
                }
                return null;
            }
        return null;
    }

    private class PlayerListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void playerJoin(PlayerCharacterFinishLoadingEvent e) {
            updateZone(e.getPlayer());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void playerQuit(PlayerCharacterLogoutEvent e) {
            currentZone.remove(e.getPlayer().getUniqueId());
        }
    }
}
