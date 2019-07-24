package com.legendsofvaleros.modules.regions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;
import com.legendsofvaleros.modules.regions.commands.RegionCommands;
import com.legendsofvaleros.modules.regions.core.Region;
import com.legendsofvaleros.modules.regions.core.RegionBounds;
import com.legendsofvaleros.modules.regions.core.RegionSelector;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import com.legendsofvaleros.modules.regions.integration.HearthstonesIntegration;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@IntegratesWith(module = HearthstoneController.class, integration = HearthstonesIntegration.class)
@ModuleInfo(name = "Regions", info = "")
public class RegionController extends RegionsAPI {
    private static RegionController instance;

    public static RegionController getInstance() {
        return instance;
    }

    public static boolean REGION_DEBUG = false;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        registerEvents(new RegionSelector());

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new RegionCommands());
    }

    public void removeRegion(String region_id) {
        if (!regions.containsKey(region_id)) {
            return;
        }

        Region region = regions.get(region_id);
        RegionBounds bounds = region.getBounds();

        for (int x = bounds.getStartX(); x < bounds.getEndX(); x++) {
            for (int y = bounds.getStartY(); y < bounds.getEndY(); y++) {
                for (int z = bounds.getStartZ(); z < bounds.getEndZ(); z++) {
                    Chunk chunk = region.world.getChunkAt(new Location(region.world, x, y, z));
                    String chunkId = chunk.getX() + "," + chunk.getZ();
                    if (regionChunks.containsEntry(chunkId, region.id)) {
                        regionChunks.remove(chunkId, region.id);
                    }
                }
            }
        }

        for (Map.Entry<Player, String> e : playerRegions.entries()) {
            if (region.id.equals(e.getValue())) {
                playerRegions.remove(e.getKey(), e.getValue());
            }
        }

        this.deleteRegion(region);
    }


    public void setRegionAccessibility(PlayerCharacter pc, String region, boolean accessible) {
        boolean current = (playerAccess.contains(pc.getUniqueCharacterId(), region) ? playerAccess.get(pc.getUniqueCharacterId(), region) : false);
        if (accessible == current) {
            return;
        }

        playerAccess.put(pc.getUniqueCharacterId(), region, accessible);
    }

    public Region getRegion(String region_id) {
        return regions.get(region_id);
    }

    public Collection<String> getPlayerRegions(Player p) {
        return playerRegions.get(p);
    }

    public boolean canPlayerAccess(CharacterId characterId, String regionId) {
        return playerAccess.get(characterId, regionId);
    }


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
        if (!toRegions.isEmpty()) {
            if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) {
                MessageUtil.sendError(event.getPlayer(), toRegions.get(0).msgError);
                event.getPlayer().teleport(event.getFrom());
                return;
            }

            PlayerCharacter pc = Characters.getPlayerCharacter(event.getPlayer());
            for (Region region : toRegions) {
                if (!region.allowAccess && (!playerAccess.contains(pc.getUniqueCharacterId(), region.id)
                        || !playerAccess.get(pc.getUniqueCharacterId(), region.id))) {
                    MessageUtil.sendError(event.getPlayer(), region.msgError);
                    event.getPlayer().teleport(event.getFrom());
                    return;
                }
            }
        }

        List<Region> discrepancies = findRegions(event.getFrom());

        for (Region region : toRegions) {
            if (!discrepancies.isEmpty()) {
                discrepancies.remove(region);
            }

            if (playerRegions.containsEntry(event.getPlayer(), region.id)) {
                continue;
            }

            if (RegionController.REGION_DEBUG) {
                event.getPlayer().sendMessage("Entered regions: " + region.id);
            }

            if (region.msgEnter != null && region.msgEnter.length() > 0) {
                MessageUtil.sendInfo(event.getPlayer(), region.msgEnter);
            }

            playerRegions.put(event.getPlayer(), region.id);
            Bukkit.getServer().getPluginManager().callEvent(new RegionEnterEvent(event.getPlayer(), region));
        }

        // Lessens checks for users in no regions
        for (Region region : discrepancies) {
            if (RegionController.REGION_DEBUG) {
                event.getPlayer().sendMessage("Left regions: " + region.id);
            }

            if (region.msgExit != null && region.msgExit.length() > 0) {
                MessageUtil.sendInfo(event.getPlayer(), region.msgExit);
            }

            playerRegions.remove(event.getPlayer(), region.id);
            Bukkit.getServer().getPluginManager().callEvent(new RegionLeaveEvent(event.getPlayer(), region));
        }
    }
}