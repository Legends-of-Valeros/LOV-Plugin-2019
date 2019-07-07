package com.codingforcookies.robert.slot;

import com.codingforcookies.robert.core.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * 
 * @author Stumblinbear
 *
 */
public interface ISlotAction {
	/**
	 * @param p The player who clicked the slot.
	 */
    void doAction(GUI gui, Player p, InventoryClickEvent event);
}