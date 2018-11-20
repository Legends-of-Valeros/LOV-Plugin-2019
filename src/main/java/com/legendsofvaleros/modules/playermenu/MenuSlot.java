package com.legendsofvaleros.modules.playermenu;

import com.codingforcookies.robert.slot.ISlotAction;
import org.bukkit.inventory.ItemStack;

public class MenuSlot {
	ItemStack stack;
	ISlotAction action;
	
	public MenuSlot(ItemStack stack, ISlotAction action) {
		this.stack = stack;
		this.action = action;
	}
}