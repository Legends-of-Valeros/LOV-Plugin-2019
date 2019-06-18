package com.legendsofvaleros.modules.professions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.professions.commands.NodeEditCommand;
import com.legendsofvaleros.modules.professions.gathering.mining.MiningNode;
import com.legendsofvaleros.modules.professions.gathering.mining.MiningTier;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.modules.zones.event.ZoneActivateEvent;
import com.legendsofvaleros.modules.zones.event.ZoneDeactivateEvent;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
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
public class ProfessionsController extends ProfessionsAPI {

    private static ProfessionsController instance;
    public ArrayList<PlayerCharacter> editModePlayers = new ArrayList<>();
    public ArrayList<Slime> slimes = new ArrayList<>();

    public static ProfessionsController getInstance() {
        if (instance == null) {
            instance = new ProfessionsController();
        }
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;

        super.onLoad();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new NodeEditCommand());

        getScheduler().executeInMyCircleTimer(() -> {
            for (List<MiningNode> nodes : this.zoneMiningNodes.values()) {
                for (MiningNode node : nodes) {
                    node.removeGlowing();
                }
            }
            //TODO get the nodes that got destroyed and respawn them
        }, 20L, 20L);
    }

    @Override
    public void onUnload() {
        for (List<MiningNode> nodes : this.zoneMiningNodes.values()) {
            for (MiningNode node : nodes) {
                node.removeGlowing();
            }
        }
        super.onUnload();

        instance = null;
    }

    @EventHandler
    public void onZoneActivate(ZoneActivateEvent event) {
        //Gathering stuff
        if (! this.zoneMiningNodes.containsKey(event.getZone().id)) {
            this.loadNodesByZone(event.getZone());
        }

        if (! this.zoneHerbalismnNodes.containsKey(event.getZone().id)) {
//            this.loadNodesByZone(event.getZone());
        }

        if (! this.zoneSkinningNodes.containsKey(event.getZone().id)) {
//            this.loadNodesByZone(event.getZone());
        }
    }

    @EventHandler
    public void onZoneDeactivate(ZoneDeactivateEvent event) {
        //Gathering stuff
        this.zoneMiningNodes.remove(event.getZone().id);

        this.zoneHerbalismnNodes.remove(event.getZone().id);

        this.zoneSkinningNodes.remove(event.getZone().id);
    }

    @EventHandler
    public void onProfessionBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (! Characters.isPlayerCharacterLoaded(player)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        Block block = event.getBlock();
        if (! MiningTier.getOreMaterials().contains(block.getType())) {
            return;
        }
        Zone zone = ZonesController.getInstance().getZone(playerCharacter);
        MiningNode destroyedNode = null;
        for (MiningNode node : this.zoneMiningNodes.get(zone.id)) {
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
            this.removeMiningNode(destroyedNode);
            MessageUtil.sendInfo(player, "Successfully removed node!");
            return;
        }
        //TODO get the tool that the block got destroyed with
        //TODO get the players profession
        event.setCancelled(true);
        destroyedNode.removeGlowing();
        destroyedNode.getLocation().getBlock().setType(Material.STONE);
        destroyedNode.setDestroyedAt(System.currentTimeMillis() / 1000L);
    }

    @EventHandler
    public void onProfessionBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (! Characters.isPlayerCharacterLoaded(player)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        if (editModePlayers.contains(playerCharacter)) {
            Block block = event.getBlock();
            if (! MiningTier.getOreMaterials().contains(block.getType())) {
                return;
            }
            MiningTier tier = MiningTier.getTier(block.getType());
            if (tier == null) {
                MessageUtil.sendError(Bukkit.getConsoleSender(), "Could not find mining tier of block type: " + block.getType());
                return;
            }
            MiningNode node = new MiningNode(block.getLocation(), ZonesController.getInstance().getZone(playerCharacter).id, tier.ordinal());
            this.saveMiningNode(node);
            MessageUtil.sendInfo(player, "Successfully saved node!");
        }
    }

    private void onMininBlockbreak() {
        //TODO
    }

    private void onHerbalismBlockbreak() {
        //TODO
    }

    @EventHandler
    public void onPlayerQuit(PlayerCharacterLogoutEvent event) {
        editModePlayers.remove(event.getPlayerCharacter());
    }

    @EventHandler
    public void onZoneEnter(ZoneEnterEvent event) {
        if (! LegendsOfValeros.getMode().allowEditing()) {
            return;
        }
        Player player = event.getPlayer();
        if (! Characters.isPlayerCharacterLoaded(player)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        if (editModePlayers.contains(playerCharacter)) {
            for (MiningNode node : this.zoneMiningNodes.get(event.getZone().id)) {
                node.setGlowing();
            }
        }
    }

    @EventHandler
    public void onZoneLeave(ZoneLeaveEvent event) {
        if (! LegendsOfValeros.getMode().allowEditing()) {
            return;
        }
        Player player = event.getPlayer();
        if (! Characters.isPlayerCharacterLoaded(player)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        if (editModePlayers.contains(playerCharacter)) {
            for (MiningNode node : this.zoneMiningNodes.get(event.getZone().id)) {
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
        if (! (event.getDamager() instanceof Player)) {
            return;
        }
        if (((Player) event.getDamager()).getGameMode() != GameMode.CREATIVE) {
            return;
        }
        if (! (event.getEntity() instanceof Slime)) {
            return;
        }
        if (! Characters.isPlayerCharacterLoaded((Player) event.getDamager())) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter((Player) event.getDamager());
        if (! editModePlayers.contains(playerCharacter)) {
            return;
        }
        slimes.remove(event.getEntity());
        event.getEntity().remove();
    }


    @EventHandler
    public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
        PhaseLock lock = event.getLock("Bank");

        onLogin(event.getPlayerCharacter().getUniqueCharacterId())
                .onFailure((err) -> MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err))
                .on(lock::release);
    }

    @EventHandler
    public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
        PhaseLock lock = event.getLock("Bank");

        onLogout(event.getPlayerCharacter().getUniqueCharacterId())
                .onFailure((err) -> MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err))
                .on(lock::release);
    }

    @EventHandler
    public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
        onDelete(event.getPlayerCharacter().getUniqueCharacterId())
                .onFailure((err) -> MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err));
    }

}
