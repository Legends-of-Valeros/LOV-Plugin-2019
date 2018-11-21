package com.legendsofvaleros.modules.gear.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class InventoryListener implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryMove(InventoryClickEvent e) {
		if(e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD
				|| e.getAction() == InventoryAction.HOTBAR_SWAP
				|| e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			e.setCancelled(true);
			return;
		}
	}
	/*
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryMove(InventoryClickEvent e) {
		if(e.getAction() == InventoryAction.NOTHING) return;

		if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
				|| e.getAction() == InventoryAction.HOTBAR_SWAP
				|| e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD
				|| e.getAction() == InventoryAction.COLLECT_TO_CURSOR
				|| e.getAction() == InventoryAction.CLONE_STACK) {
			e.setCancelled(true);
			return;
		}

		if(e.getAction().name().startsWith("DROP_")) return;

		if(!Characters.isPlayerCharacterLoaded((Player)e.getWhoClicked())) return;

		GearItem.Instance onCursor = GearItem.Instance.fromStack(e.getCursor());

		GearItem.Instance inSlot = GearItem.Instance.fromStack(e.getCurrentItem());

		if(onCursor == null && inSlot == null) return;

		InventoryAction action = e.getAction();

		if(action == InventoryAction.SWAP_WITH_CURSOR) {
			// Ignore swaps of different items
			if(!onCursor.gear.isSimilar(inSlot))
				return;

			action = e.isRightClick() ? InventoryAction.PLACE_ONE : InventoryAction.PLACE_ALL;
		}

		// Not acting on similar items. Cancel.
			if((onCursor != null && inSlot != null && !onCursor.gear.isSimilar(inSlot))
				|| (onCursor != null && inSlot != null && !inSlot.gear.isSimilar(onCursor))) {
			e.setCancelled(true);
			return;
		}

		boolean isNew = onCursor == null || inSlot == null;
		boolean place = action.name().startsWith("PLACE_");

		boolean some = action.name().endsWith("_SOME");
		boolean half = action.name().endsWith("_HALF");
		boolean one = action.name().endsWith("_ONE");

		int change = (one ? 1 :
				(place ? onCursor.amount :
						(half ? inSlot.amount / 2 : inSlot.amount)));

		boolean all;

		// Clamp the value to the current stack size
			if(place) {
			// If merging stacks, only allow up to the max stack size
			if(!isNew)
				change = Math.min(change, onCursor.gear.getMaxAmount() - inSlot.amount);
			all = change == onCursor.amount;
		}else{
			// If merging stacks, only allow up to the max stack size
			if(!isNew)
				change = Math.min(change, inSlot.gear.getMaxAmount() - onCursor.amount);
			all = change == inSlot.amount;
		}

		if(change == 0) {
			e.setCancelled(true);
			return;
		}

		if(place) {
			if(isNew) {
				if(all) {
					inSlot = onCursor;
					onCursor = null;
				}else{
					inSlot = onCursor.copy();
					inSlot.amount = change;

					onCursor.amount -= change;
				}
			}else{
				inSlot.amount += change;
				onCursor.amount -= change;
			}
		}else{
			if(isNew) {
				if(all) {
					onCursor = inSlot;
					inSlot = null;
				}else{
					onCursor = inSlot.copy();
					onCursor.amount = change;

					inSlot.amount -= change;
				}
			}else{
				onCursor.amount += change;
				inSlot.amount -= change;
			}
		}

		e.getView().setCursor(onCursor != null ? onCursor.toStack() : null);
		e.setCurrentItem(inSlot != null ? inSlot.toStack() : null);

		// ((Player)e.getWhoClicked()).updateInventory();

		e.setCancelled(true);
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		// Cancel anything not in the player's inventory.
		for(Integer slot : e.getRawSlots()) {
			// Raw slots >= than the inventory size is within the player's inventory
			if(slot < e.getInventory().getSize()) {
				e.setCancelled(true);
				return;
			}
		}

		GearItem.Instance getInstance = GearItem.Instance.fromStack(e.getCursor());
		if(getInstance == null) return;

		int i = 0;

		for(Map.Entry<Integer, ItemStack> entry : e.getNewItems().entrySet()) {
			i += entry.getValue().getAmount();

			GearItem.Instance ii = getInstance.copy();

			ii.amount = entry.getValue().getAmount();
			getInstance.amount -= ii.amount;

			// Copy the stack into the new slots
			e.getWhoClicked().getInventory().setItem(entry.getKey(), ii.toStack());
		}

		if(e.getCursor() != null)
			e.setCursor(getInstance.toStack());
	}*/

	@EventHandler
	public void onSwapItems(PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
	}
}