package com.legendsofvaleros.modules.professions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.professions.commands.NodeEditCommand;
import com.legendsofvaleros.modules.professions.gathering.GatheringNode;
import com.legendsofvaleros.modules.professions.gathering.mining.MiningNode;
import com.legendsofvaleros.modules.professions.gathering.mining.MiningTier;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.modules.zones.event.ZoneActivateEvent;
import com.legendsofvaleros.modules.zones.event.ZoneDeactivateEvent;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 02/12/2019
 */
@DependsOn(ZonesController.class)
public class ProfessionsController extends ProfessionsAPI {

    private static ProfessionsController instance;
    public List<PlayerCharacter> editModePlayers = new ArrayList<>();

    public static ProfessionsController getInstance() {
        if (instance == null) {
            instance = new ProfessionsController();
        }
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new NodeEditCommand());

//        getScheduler().executeInMyCircleTimer(() -> {
//            for (List<GatheringNode> nodes : this.zoneGatheringNodes.values()) {
//                for (GatheringNode node : nodes) {
//                    node.removeGlowing();
//                }
//            }
//            //TODO get the nodes that got destroyed and respawn them
//        }, 20L, 20L);
    }

    @Override
    public void onUnload() {
        for (List<GatheringNode> nodes : this.zoneGatheringNodes.values()) {
            for (GatheringNode node : nodes) {
                node.removeGlowing();
            }
        }
        instance = null;
        super.onUnload();
    }

    @EventHandler
    public void onZoneActivate(ZoneActivateEvent event) {
        //Gathering stuff
        if (!this.zoneGatheringNodes.containsKey(event.getZone().id)) {
            this.loadNodesByZone(event.getZone());
        }

    }

    @EventHandler
    public void onZoneDeactivate(ZoneDeactivateEvent event) {
        //Gathering stuff
        this.zoneGatheringNodes.remove(event.getZone().id);
    }

    @EventHandler
    public void onProfessionBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Characters.isPlayerCharacterLoaded(player)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        Block block = event.getBlock();
        //TODO check if it is a gathering node
        if (!MiningTier.getOreMaterials().contains(block.getType())) {
            return;
        }
        Zone zone = ZonesController.getInstance().getZone(playerCharacter).getZone();
        GatheringNode destroyedNode = null;
        for (GatheringNode node : this.zoneGatheringNodes.get(zone.id)) {
            if (node.getLocation().equals(block.getLocation())) {
                destroyedNode = node;
                break;
            }
        }
        if (destroyedNode == null) {
            return;
        }

        //If player is in edit mode, then remove the node from the database
        if (editModePlayers.contains(playerCharacter)) {
            this.removeGatheringNode(destroyedNode);
            MessageUtil.sendInfo(player, "Successfully removed " + destroyedNode.getType() + "node!");
            return;
        }
        //TODO get the tool that the block got destroyed with
        //TODO get the players profession
        //TODO check if the player is able to mine the current node
        event.setCancelled(true);
        destroyedNode.removeGlowing();
        destroyedNode.getLocation().getBlock().setType(Material.STONE);
        destroyedNode.setDestroyedAt(System.currentTimeMillis() / 1000L);
    }

    @EventHandler
    public void onProfessionBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!Characters.isPlayerCharacterLoaded(player)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        if (editModePlayers.contains(playerCharacter)) {
            Block block = event.getBlock();
            if (!MiningTier.getOreMaterials().contains(block.getType())) {
                return;
            }
            MiningTier tier = MiningTier.getTier(block.getType());
            if (tier == null) {
                MessageUtil.sendError(Bukkit.getConsoleSender(), "Could not find mining tier of block type: " + block.getType());
                return;
            }
            this.saveGatheringNode(new MiningNode(block.getLocation(), ZonesController.getInstance().getZone(playerCharacter).getZone().id, tier.ordinal()));
            MessageUtil.sendInfo(player, "Successfully saved node!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerCharacterLogoutEvent event) {
        editModePlayers.remove(event.getPlayerCharacter());
    }

    @EventHandler
    public void onZoneEnter(ZoneEnterEvent event) {
        if (!LegendsOfValeros.getMode().allowEditing()) {
            return;
        }
        Player player = event.getPlayer();
        if (!Characters.isPlayerCharacterLoaded(player)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        if (editModePlayers.contains(playerCharacter)) {
            for (GatheringNode node : this.zoneGatheringNodes.get(event.getZone().id)) {
                node.setGlowing();
            }
        }
    }

    @EventHandler
    public void onZoneLeave(ZoneLeaveEvent event) {
        if (!LegendsOfValeros.getMode().allowEditing()) {
            return;
        }
        Player player = event.getPlayer();
        if (!Characters.isPlayerCharacterLoaded(player)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        if (editModePlayers.contains(playerCharacter)) {
            for (GatheringNode node : this.zoneGatheringNodes.get(event.getZone().id)) {
                node.removeGlowing();
            }
        }
    }

    /**
     * Adds the possibility to remove the glow effect with left click
     * @param event
     */
    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (((Player) event.getDamager()).getGameMode() != GameMode.CREATIVE) {
            return;
        }
        if (!(event.getEntity() instanceof Slime)) {
            return;
        }
        if (!Characters.isPlayerCharacterLoaded((Player) event.getDamager())) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter((Player) event.getDamager());
        if (!editModePlayers.contains(playerCharacter)) {
            return;
        }
        //TODO check if the slime is part of a highlighted entity
        event.getEntity().remove();
    }


    @EventHandler
    public void onCharacterLoad(PlayerCharacterStartLoadingEvent event) {
        PhaseLock lock = event.getLock("Professions");

        onLogin(event.getPlayerCharacter().getUniqueCharacterId())
                .onFailure(err -> {
                    MessageUtil.sendSevereException(ProfessionsController.getInstance(), event.getPlayer(), err);
                    getScheduler().executeInSpigotCircle(new InternalTask(() -> {
                        event.getPlayer().kickPlayer("Failed loading PlayerProfession - If this error persists, try contacting the support");
                    }));
                })
                .on(lock::release);
    }

    @EventHandler
    public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
        onLogout(event.getPlayerCharacter().getUniqueCharacterId())
                .onFailure(err -> MessageUtil.sendSevereException(ProfessionsController.getInstance(), event.getPlayer(), err));
    }

    @EventHandler
    public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
        onDelete(event.getPlayerCharacter().getUniqueCharacterId())
                .onFailure(err -> MessageUtil.sendSevereException(ProfessionsController.getInstance(), event.getPlayer(), err));
    }

}
