package com.codingforcookies.robert.slot;

import com.codingforcookies.robert.core.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SlotClose implements ISlotAction {
	public void doAction(GUI gui, Player p, InventoryClickEvent event) { gui.close(p); }
}