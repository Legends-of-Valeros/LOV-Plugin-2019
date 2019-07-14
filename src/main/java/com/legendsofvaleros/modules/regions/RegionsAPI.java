package com.legendsofvaleros.modules.regions;

import com.google.common.collect.*;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.regions.core.Region;
import com.legendsofvaleros.modules.regions.core.RegionBounds;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionsAPI extends ModuleListener {
    public interface RPC {
        Promise<List<Region>> findRegions();

        Promise<Boolean> saveRegion(Region region);

        Promise<Boolean> deleteRegion(String id);

        Promise<Map<String, Boolean>> getPlayerRegionAccess(CharacterId characterId);

        Promise<Boolean> savePlayerRegionAccess(CharacterId characterId, Map<String, Boolean> map);

        Promise<Boolean> deletePlayerRegionAccess(CharacterId characterId);

        Promise<Boolean> deletePlayerRegionAccess(CharacterId characterId, String regionId);
    }

    private RPC rpc;
    private static final List<Region> EMPTY_LIST = ImmutableList.of();
    protected HashMap<String, Region> regions = new HashMap<>();

    /**
     * A map who's key is a chunk x,y pair and the regions inside it. Allows for extremely fast regions searching.
     */
    Multimap<String, String> regionChunks = HashMultimap.create();
    Table<CharacterId, String, Boolean> playerAccess = HashBasedTable.create();
    Multimap<Player, String> playerRegions = HashMultimap.create();


    @Override
    public void onLoad() {
        super.onLoad();
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
        }, RegionController.getInstance().getScheduler()::sync).onFailure(Throwable::printStackTrace);
    }

    public List<Region> findRegions(Location location) {
        String chunkId = location.getChunk().getX() + "," + location.getChunk().getZ();
        if (!regionChunks.containsKey(chunkId))
            return EMPTY_LIST;

        List<Region> foundRegions = new ArrayList<>();

        for (String regionId : regionChunks.get(chunkId)) {
            Region region = regions.get(regionId);

            if (region.isInside(location))
                foundRegions.add(region);
        }

        return foundRegions;
    }

    public Promise<Boolean> saveRegion(Region region) {
        return rpc.saveRegion(region);
    }

    public void addRegion(Region region) {
        if (regions.containsKey(region.id))
            return;

        if (region.world == null) {
            MessageUtil.sendException(RegionController.getInstance(), "Region has a null world. Offender: " + region.id);
            return;
        }

        regions.put(region.id, region);
        RegionBounds bounds = region.getBounds();

        for (int x = bounds.getStartX(); x <= bounds.getEndX(); x++)
            for (int y = bounds.getStartY(); y <= bounds.getEndY(); y++)
                for (int z = bounds.getStartZ(); z <= bounds.getEndZ(); z++) {
                    Chunk chunk = region.world.getChunkAt(new Location(region.world, x, y, z));
                    String chunkId = chunk.getX() + "," + chunk.getZ();
                    if (!regionChunks.containsEntry(chunkId, region.id))
                        regionChunks.put(chunkId, region.id);
                }
    }

    public void deleteRegion(Region region) {
        getScheduler().executeInMyCircle(() -> {
            rpc.deleteRegion(region.id).onSuccess(() -> {
                regions.remove(region.id);
            }).onFailure(Throwable::printStackTrace);
        });
    }

    public Promise<Map<String, Boolean>> onLogin(PlayerCharacter pc) {
        return rpc.getPlayerRegionAccess(pc.getUniqueCharacterId()).onSuccess(val -> {
            val.orElse(ImmutableMap.of()).entrySet().forEach(entry ->
                    playerAccess.put(pc.getUniqueCharacterId(), entry.getKey(), entry.getValue()));
        });
    }

    public Promise<Boolean> onLogout(PlayerCharacter pc) {
        return rpc.savePlayerRegionAccess(pc.getUniqueCharacterId(), playerAccess.row(pc.getUniqueCharacterId())).on(() -> {
            playerRegions.removeAll(pc.getPlayer());

            playerAccess.row(pc.getUniqueCharacterId()).clear();
        });
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerRegionAccess(characterId);
    }

}