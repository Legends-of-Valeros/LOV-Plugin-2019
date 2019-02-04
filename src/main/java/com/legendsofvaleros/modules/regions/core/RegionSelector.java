package com.legendsofvaleros.modules.regions.core;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class RegionSelector implements Listener {
    public static String ITEM_NAME = "Region Selector";
    public static HashMap<Player, Location[]> selection = new HashMap<>();

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
                            event.getPlayer().sendMessage("Set regions location 1.");
                        }
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (selection.get(event.getPlayer())[1] != event.getClickedBlock().getLocation()) {
                            selection.get(event.getPlayer())[1] = event.getClickedBlock().getLocation();
                            event.getPlayer().sendMessage("Set regions location 2.");
                        }
                    }

                    event.setCancelled(true);
                }
            }
        }
    }
}
