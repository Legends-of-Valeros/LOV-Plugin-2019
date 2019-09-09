package com.legendsofvaleros.modules.regions;

import com.google.common.collect.*;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.regions.core.IRegion;
import com.legendsofvaleros.modules.regions.core.Region;
import com.legendsofvaleros.modules.regions.core.RegionBounds;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionsAPI extends ListenerModule {
    public interface RPC {
        Promise<List<Region>> findRegions();

        Promise<Object> saveRegion(IRegion region);

        Promise<Boolean> deleteRegion(String id);

        Promise<Map<String, Boolean>> getPlayerRegionAccess(CharacterId characterId);

        Promise<Object> savePlayerRegionAccess(CharacterId characterId, Map<String, Boolean> map);

        Promise<Boolean> deletePlayerRegionAccess(CharacterId characterId);

        Promise<Boolean> deletePlayerRegionAccess(CharacterId characterId, String regionId);
    }

    private RPC rpc;
    private static final List<IRegion> EMPTY_LIST = ImmutableList.of();
    protected Map<String, IRegion> regions = new HashMap<>();

    /**
     * A map who's key is a chunk x,y pair and the regions inside it. Allows for extremely fast regions searching.
     */
    Multimap<String, String> regionChunks = HashMultimap.create();
    Table<CharacterId, String, Boolean> playerAccess = HashBasedTable.create();
    Multimap<Player, String> playerRegions = HashMultimap.create();


    @Override
    public void onLoad() {
        super.onLoad();

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(IRegion.class, new TypeAdapter<IRegion>() {
                    @Override
                    public void write(JsonWriter write, IRegion region) throws IOException {
                        write.value(region != null ? region.getId() : null);
                    }

                    @Override
                    public IRegion read(JsonReader read) throws IOException {
                        // If we reference the interface, then the type should be a string, and we return the stored object.
                        // Note: it must be loaded already, else this returns null.
                        if(read.peek() == JsonToken.NULL) {
                            read.nextNull();
                            return null;
                        }

                        return regions.get(read.nextString());
                    }
                });
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
        this.rpc = APIController.create(RPC.class);

        try {
            this.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public Promise<List<Region>> loadAll() {
        return rpc.findRegions().onSuccess(val -> {
            regionChunks.clear();
            regions.clear();

            val.orElse(ImmutableList.of()).forEach(this::addRegion);

            getLogger().info("Loaded " + regions.size() + " regions.");
        }, RegionController.getInstance().getScheduler()::sync);
    }

    public List<IRegion> findRegions(Location location) {
        String chunkId = location.getChunk().getX() + "," + location.getChunk().getZ();
        if (!regionChunks.containsKey(chunkId)) {
            return EMPTY_LIST;
        }

        List<IRegion> foundRegions = new ArrayList<>();

        for (String regionId : regionChunks.get(chunkId)) {
            IRegion region = regions.get(regionId);

            if (region.isInside(location)) {
                foundRegions.add(region);
            }
        }

        return foundRegions;
    }

    public Promise saveRegion(IRegion region) {
        return rpc.saveRegion(region);
    }

    public void addRegion(IRegion region) {
        if (regions.containsKey(region.getId()))
            return;

        if (region.getWorld() == null) {
            MessageUtil.sendException(RegionController.getInstance(), "Region has a null world. Offender: " + region.getId());
            return;
        }

        regions.put(region.getId(), region);
        RegionBounds bounds = region.getBounds();

        for (int x = bounds.getStartX(); x <= bounds.getEndX(); x++) {
            for (int y = bounds.getStartY(); y <= bounds.getEndY(); y++) {
                for (int z = bounds.getStartZ(); z <= bounds.getEndZ(); z++) {
                    Chunk chunk = region.getWorld().getChunkAt(new Location(region.getWorld(), x, y, z));
                    String chunkId = chunk.getX() + "," + chunk.getZ();

                    if (!regionChunks.containsEntry(chunkId, region.getId())) {
                        regionChunks.put(chunkId, region.getId());
                    }
                }
            }
        }
    }

    public void deleteRegion(IRegion region) {
        getScheduler().executeInMyCircle(() -> {
            rpc.deleteRegion(region.getId()).onSuccess(() -> {
                regions.remove(region.getId());
            });
        });
    }

    public Promise<Map<String, Boolean>> onLogin(PlayerCharacter pc) {
        return rpc.getPlayerRegionAccess(pc.getUniqueCharacterId()).onSuccess(val -> {
            val.orElse(ImmutableMap.of()).forEach((key, value) -> playerAccess.put(pc.getUniqueCharacterId(), key, value));
        });
    }

    public Promise onLogout(PlayerCharacter pc) {
        return rpc.savePlayerRegionAccess(pc.getUniqueCharacterId(), playerAccess.row(pc.getUniqueCharacterId())).on(() -> {
            playerRegions.removeAll(pc.getPlayer());

            playerAccess.row(pc.getUniqueCharacterId()).clear();
        });
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerRegionAccess(characterId);
    }

}