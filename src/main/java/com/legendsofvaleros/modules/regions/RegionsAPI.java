package com.legendsofvaleros.modules.regions;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.regions.core.IRegion;
import com.legendsofvaleros.modules.regions.core.PlayerAccessibility;
import com.legendsofvaleros.modules.regions.core.Region;
import com.legendsofvaleros.modules.regions.core.RegionBounds;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class RegionsAPI extends ListenerModule {
    public interface RPC {
        Promise<List<Region>> findRegions();

        Promise<Object> saveRegion(IRegion region);

        Promise<Boolean> deleteRegion(String id);

        Promise<PlayerAccessibility> getPlayerRegionAccess(CharacterId characterId);

        Promise<Object> savePlayerRegionAccess(PlayerAccessibility accessibility);

        Promise<Boolean> deletePlayerRegionAccess(CharacterId characterId);
    }

    private RPC rpc;
    private static final List<IRegion> EMPTY_LIST = ImmutableList.of();
    protected Map<String, IRegion> regions = new HashMap<>();

    /**
     * A map who's key is a chunk x,y pair and the regions inside it. Allows for extremely fast regions searching.
     */
    Multimap<String, String> regionChunks = HashMultimap.create();
    Map<CharacterId, PlayerAccessibility> playerAccess = new HashMap<>();
    Multimap<Player, IRegion> playerRegions = HashMultimap.create();

    public IRegion getRegion(String region_id) {
        return regions.get(region_id);
    }

    public boolean canPlayerAccess(CharacterId characterId, IRegion region) {
        return playerAccess.get(characterId).hasAccess(region);
    }

    public void setRegionAccessibility(PlayerCharacter pc, IRegion region, boolean accessible) {
        playerAccess.get(pc.getUniqueCharacterId()).setAccessibility(region, accessible);
    }

    public Collection<IRegion> getPlayerRegions(Player p) {
        return playerRegions.get(p);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        InterfaceTypeAdapter.register(IRegion.class,
                                        obj -> obj.getId(),
                                        id -> regions.get(id));
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

    public Promise onLogin(PlayerCharacter pc) {
        return rpc.getPlayerRegionAccess(pc.getUniqueCharacterId()).onSuccess(val -> {
            playerAccess.put(pc.getUniqueCharacterId(), val.orElseGet(() -> new PlayerAccessibility(pc)));
        });
    }

    public Promise onLogout(PlayerCharacter pc) {
        playerRegions.removeAll(pc.getPlayer());
        return rpc.savePlayerRegionAccess(playerAccess.remove(pc.getUniqueCharacterId()));
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerRegionAccess(characterId);
    }

}