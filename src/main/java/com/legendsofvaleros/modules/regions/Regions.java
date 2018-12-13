package com.legendsofvaleros.modules.regions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.hearthstones.HearthstoneCastEvent;
import com.legendsofvaleros.modules.hearthstones.Hearthstones;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.QuestActionFactory;
import com.legendsofvaleros.modules.quests.objective.stf.QuestObjectiveFactory;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import com.legendsofvaleros.modules.regions.quest.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(Hearthstones.class)
@IntegratesWith(module = Quests.class, integration = QuestIntegration.class)
public class Regions extends ModuleListener {
    private static Regions instance;
    public static Regions getInstance() { return instance; }

    public static boolean REGION_DEBUG = false;

    private static RegionManager regionManager;

    public static RegionManager manager() {
        return regionManager;
    }

    public static String ITEM_NAME = "Region Selector";
    public static HashMap<Player, Location[]> selection = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        regionManager = new RegionManager();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new RegionCommands());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnterRetion(RegionEnterEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeaveRetion(RegionLeaveEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler
    public void onCastHearthstone(HearthstoneCastEvent event) {
        for (String region_id : regionManager.getPlayerRegions(event.getPlayer())) {
            if (!regionManager.getRegion(region_id).allowHearthstone) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;

        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if (hand != null) {
            if (hand.getType() == Material.ARROW) {
                if (hand.getItemMeta().getDisplayName().contains(ITEM_NAME)) {
                    if (!selection.containsKey(event.getPlayer()))
                        selection.put(event.getPlayer(), new Location[2]);

                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        if (selection.get(event.getPlayer())[0] != event.getClickedBlock().getLocation()) {
                            selection.get(event.getPlayer())[0] = event.getClickedBlock().getLocation();
                            event.getPlayer().sendMessage("Set region location 1.");
                        }
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (selection.get(event.getPlayer())[1] != event.getClickedBlock().getLocation()) {
                            selection.get(event.getPlayer())[1] = event.getClickedBlock().getLocation();
                            event.getPlayer().sendMessage("Set region location 2.");
                        }
                    }

                    event.setCancelled(true);
                }
            }
        }
    }
}