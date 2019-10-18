package com.legendsofvaleros.features.gui.slot;

import com.legendsofvaleros.features.gui.core.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author Stumblinbear
 */
public interface ISlotAction {

    /**
     * @param gui
     * @param p     The player who clicked the slot.
     * @param event
     */
    void doAction(GUI gui, Player p, InventoryClickEvent event);
}