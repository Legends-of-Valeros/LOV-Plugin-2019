package com.codingforcookies.robert.slot;

import com.codingforcookies.robert.core.GUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 
 * @author Stumblinbear
 *
 */
public abstract class SlotGrabbable implements ISlotAction {
	public enum GrabType {
		CLONE,
		PICKUP
	}
	
	@Override
	public void doAction(GUI gui, Player p, InventoryClickEvent event) {
		boolean hasSlotItem = event.getClickedInventory().getItem(event.getSlot()) != null;
		boolean hasCursorItem = event.getCursor() != null && event.getCursor().getType() != Material.AIR;

		if(hasCursorItem) {
			if(!attemptDrop(gui, p, event, hasSlotItem))
				return;
			
			if(hasSlotItem) {
				GrabType type = attemptPickup(gui, p, event, hasCursorItem);
				if(type == null)
					return;
				
				// Switch the two items
				ItemStack stack = event.getCursor();
				
				// TODO: Does this work?
				//event.setCursor();
				event.getView().setCursor(event.getClickedInventory().getItem(event.getSlot()));
				
				event.getClickedInventory().setItem(event.getSlot(), stack);
			}else{
				// Drop the item into the slot
				event.getClickedInventory().setItem(event.getSlot(), event.getClickedInventory().getItem(event.getSlot()));

				event.getView().setCursor(null);
			}
		}else{
			if(!hasSlotItem)
				return;
			
			GrabType type = attemptPickup(gui, p, event, hasCursorItem);
			if(type == null)
				return;
			
			event.getView().setCursor(event.getClickedInventory().getItem(event.getSlot()));
			
			if(type == GrabType.PICKUP)
				event.getClickedInventory().setItem(event.getSlot(), null);
			else if(type == GrabType.CLONE)
				event.getClickedInventory().setItem(event.getSlot(), event.getCursor());
		}
	}

	public abstract GrabType attemptPickup(GUI gui, Player p, InventoryClickEvent event, boolean hasCursorItem);
	public abstract boolean attemptDrop(GUI gui, Player p, InventoryClickEvent event, boolean hasSlotItem);
}