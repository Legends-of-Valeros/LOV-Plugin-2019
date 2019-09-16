package com.legendsofvaleros.features.gui.slot;

import com.legendsofvaleros.features.gui.core.GUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Stumblinbear
 */
public abstract class SlotUsable implements ISlotAction {
    @Override
    public void doAction(GUI gui, Player p, InventoryClickEvent event) {
        if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR
                && event.getAction() != InventoryAction.PLACE_SOME
                && event.getAction() != InventoryAction.PLACE_ONE
                && event.getAction() != InventoryAction.PLACE_ALL
                && event.getAction() != InventoryAction.PICKUP_ALL
                && event.getAction() != InventoryAction.PICKUP_HALF
                && event.getAction() != InventoryAction.PICKUP_SOME
                && event.getAction() != InventoryAction.PICKUP_ONE) {
            event.setCancelled(true);
            return;
        }

        if (event.getCurrentItem() != null)
            onPickup(gui, p, event.getCurrentItem(), event);

        if (event.getCursor().getType() != Material.AIR)
            onPlace(gui, p, event.getCursor(), event);
    }

    public abstract void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent event);

    public abstract void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent event);
}