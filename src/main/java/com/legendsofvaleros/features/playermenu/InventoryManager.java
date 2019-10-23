package com.legendsofvaleros.features.playermenu;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryManager implements Listener {
    public static class InventoryItem {
        final ItemStack stack;
        final InventoryItemAction action;

        public InventoryItem(ItemStack stack, InventoryItemAction action) {
            this.stack = stack;
            this.action = action;
        }
    }

    @FunctionalInterface
    public interface InventoryItemAction {
        void onClick(Player p, InventoryClickEvent event);
    }

    private static HashMap<Integer, InventoryItem> fixedItems = new HashMap<>();

    public static boolean hasFixedItem(int slot) {
        return fixedItems.containsKey(slot);
    }

    /**
     * 40 - 45 = Crafting slots
     * @param slot
     * @param item
     */
    public static void addFixedItem(int slot, InventoryItem item) {
        fixedItems.put(slot, item);
    }

    public static void fillInventory(Player player) {
        for (Entry<Integer, InventoryItem> item : fixedItems.entrySet()) {
            if (item.getValue() != null && item.getValue().stack != null) {
                player.getInventory().setItem(item.getKey(), item.getValue().stack);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }

        int slot = -1;

        if (event.getClickedInventory().getType() == InventoryType.CRAFTING) {
            slot = 41 + event.getSlot();
        } else if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            slot = event.getSlot();
        }

        if (fixedItems.containsKey(slot)) {
            event.setCancelled(true);

            Player p = (Player) event.getWhoClicked();

            InventoryItem item = fixedItems.get(slot);
            if (item != null && item.action != null) {
                item.action.onClick(p, event);
            }
        }
    }
}