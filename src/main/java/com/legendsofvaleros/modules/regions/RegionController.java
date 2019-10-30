package com.legendsofvaleros.modules.regions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;
import com.legendsofvaleros.modules.regions.commands.RegionCommands;
import com.legendsofvaleros.modules.regions.core.IRegion;
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

        IRegion region = regions.get(region_id);
        RegionBounds bounds = region.getBounds();

        for (int x = bounds.getStartX(); x < bounds.getEndX(); x++) {
            for (int y = bounds.getStartY(); y < bounds.getEndY(); y++) {
                for (int z = bounds.getStartZ(); z < bounds.getEndZ(); z++) {
                    Chunk chunk = region.getWorld().getChunkAt(new Location(region.getWorld(), x, y, z));
                    String chunkId = chunk.getX() + "," + chunk.getZ();
                    if (regionChunks.containsEntry(chunkId, region.getId())) {
                        regionChunks.remove(chunkId, region.getId());
                    }
                }
            }
        }

        for (Map.Entry<Player, IRegion> e : playerRegions.entries()) {
            if (e.getValue() == region) {
                playerRegions.remove(e.getKey(), e.getValue());
            }
        }

        this.deleteRegion(region);
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

        List<IRegion> toRegions = findRegions(event.getTo());
        if (!toRegions.isEmpty()) {
            if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) {
                MessageUtil.sendError(event.getPlayer(), toRegions.get(0).getDenyMessage());
                event.getPlayer().teleport(event.getFrom());
                return;
            }

            PlayerCharacter pc = Characters.getPlayerCharacter(event.getPlayer());
            for (IRegion region : toRegions) {
                if (!playerAccess.get(pc.getUniqueCharacterId()).hasAccess(region)) {
                    MessageUtil.sendError(event.getPlayer(), region.getDenyMessage());
                    event.getPlayer().teleport(event.getFrom());
                    return;
                }
            }
        }

        List<IRegion> discrepancies = findRegions(event.getFrom());

        for (IRegion region : toRegions) {
            if (!discrepancies.isEmpty()) {
                discrepancies.remove(region);
            }

            if (playerRegions.containsEntry(event.getPlayer(), region.getId())) {
                continue;
            }

            if (RegionController.REGION_DEBUG) {
                event.getPlayer().sendMessage("Entered regions: " + region.getId());
            }

            if (region.getEnterMessage() != null && region.getEnterMessage().length() > 0) {
                MessageUtil.sendInfo(event.getPlayer(), region.getEnterMessage());
            }

            playerRegions.put(event.getPlayer(), region);
            Bukkit.getServer().getPluginManager().callEvent(new RegionEnterEvent(event.getPlayer(), region));
        }

        // Lessens checks for users in no regions
        for (IRegion region : discrepancies) {
            if (RegionController.REGION_DEBUG) {
                event.getPlayer().sendMessage("Left regions: " + region.getId());
            }

            if (region.getExitMessage() != null && region.getExitMessage().length() > 0) {
                MessageUtil.sendInfo(event.getPlayer(), region.getExitMessage());
            }

            playerRegions.remove(event.getPlayer(), region);

            Bukkit.getServer().getPluginManager().callEvent(new RegionLeaveEvent(event.getPlayer(), region));
        }
    }
}