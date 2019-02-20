package com.legendsofvaleros.modules.regions;

import com.google.common.collect.*;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.regions.core.Region;
import com.legendsofvaleros.modules.regions.core.RegionBounds;
import com.legendsofvaleros.modules.regions.core.RegionSelector;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class RegionsAPI extends Module {
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

    /**
     * A map who's key is a chunk x,y pair and the regions inside it. Allows for extremely fast regions searching.
     */
    private Multimap<String, String> regionChunks = HashMultimap.create();

    private HashMap<String, Region> regions = new HashMap<>();
    public Region getRegion(String region_id) {
        return regions.get(region_id);
    }

    private Multimap<Player, String> playerRegions = HashMultimap.create();
    public Collection<String> getPlayerRegions(Player p) {
        return playerRegions.get(p);
    }

    private Table<CharacterId, String, Boolean> playerAccess = HashBasedTable.create();
    public boolean canPlayerAccess(CharacterId characterId, String regionId) {
        return playerAccess.get(characterId, regionId);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        registerEvents(new PlayerListener());
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

    public Promise<List<Region>> loadAll() {
        return rpc.findRegions().onSuccess(val -> {
            regionChunks.clear();
            regions.clear();

            val.orElse(ImmutableList.of()).stream().forEach(this::addRegion);

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

    public void setRegionAccessibility(PlayerCharacter pc, String region, boolean accessible) {
        boolean current = (playerAccess.contains(pc.getUniqueCharacterId(), region) ? playerAccess.get(pc.getUniqueCharacterId(), region) : false);
        if (accessible == current) return;

        playerAccess.put(pc.getUniqueCharacterId(), region, accessible);
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

    public void removeRegion(String region_id) {
        if (!regions.containsKey(region_id))
            return;

        Region region = regions.get(region_id);
        RegionBounds bounds = region.getBounds();

        for (int x = bounds.getStartX(); x < bounds.getEndX(); x++)
            for (int y = bounds.getStartY(); y < bounds.getEndY(); y++)
                for (int z = bounds.getStartZ(); z < bounds.getEndZ(); z++) {
                    Chunk chunk = region.world.getChunkAt(new Location(region.world, x, y, z));
                    String chunkId = chunk.getX() + "," + chunk.getZ();
                    if (regionChunks.containsEntry(chunkId, region.id))
                        regionChunks.remove(chunkId, region.id);
                }

        for (Map.Entry<Player, String> e : playerRegions.entries())
            if (region.id.equals(e.getValue()))
                playerRegions.remove(e.getKey(), e.getValue());

        regions.remove(region_id);

        rpc.deleteRegion(region.id);
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

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLoading(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Regions");

            onLogin(event.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler
        public void onPlayerLogout(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Regions");

            onLogout(event.getPlayerCharacter()).on(lock::release);

            RegionSelector.selection.remove(event.getPlayer());
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent e) {
            onDelete(e.getPlayerCharacter().getUniqueCharacterId());
        }

        @EventHandler
        public void onMove(PlayerMoveEvent event) {
            if (event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation()))
                return;

            List<Region> toRegions = findRegions(event.getTo());
            if (toRegions.size() > 0) {
                if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) {
                    MessageUtil.sendError(event.getPlayer(), toRegions.get(0).msgFailure);
                    event.getPlayer().teleport(event.getFrom());
                    return;
                }

                PlayerCharacter pc = Characters.getPlayerCharacter(event.getPlayer());
                for(Region region : toRegions) {
                    if(!region.allowAccess) {
                        if (!playerAccess.contains(pc.getUniqueCharacterId(), region.id)
                                || !playerAccess.get(pc.getUniqueCharacterId(), region.id)) {
                            MessageUtil.sendError(event.getPlayer(), region.msgFailure);
                            event.getPlayer().teleport(event.getFrom());
                            return;
                        }
                    }
                }
            }

            List<Region> discrepancies = findRegions(event.getFrom());

            for (Region region : toRegions) {
                if(discrepancies.size() > 0)
                    discrepancies.remove(region);

                if (playerRegions.containsEntry(event.getPlayer(), region.id))
                    continue;

                if (RegionController.REGION_DEBUG)
                    event.getPlayer().sendMessage("Entered regions: " + region.id);

                if (region.msgEnter != null && region.msgEnter.length() > 0)
                    MessageUtil.sendInfo(event.getPlayer(), region.msgEnter);

                playerRegions.put(event.getPlayer(), region.id);
                Bukkit.getServer().getPluginManager().callEvent(new RegionEnterEvent(event.getPlayer(), region));
            }

            // Lessens checks for users in no regions
            for (Region region : discrepancies) {
                if (RegionController.REGION_DEBUG)
                    event.getPlayer().sendMessage("Left regions: " + region.id);

                if (region.msgExit != null && region.msgExit.length() > 0)
                    MessageUtil.sendInfo(event.getPlayer(), region.msgExit);

                playerRegions.remove(event.getPlayer(), region.id);
                Bukkit.getServer().getPluginManager().callEvent(new RegionLeaveEvent(event.getPlayer(), region));
            }
        }
    }
}