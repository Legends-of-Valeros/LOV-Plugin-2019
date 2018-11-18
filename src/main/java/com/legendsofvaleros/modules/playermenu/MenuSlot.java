package com.legendsofvaleros.modules.playermenu;

import org.bukkit.inventory.ItemStack;

import com.codingforcookies.robert.slot.ISlotAction;

public class MenuSlot {
	ItemStack stack;
	ISlotAction action;
	
	public MenuSlot(ItemStack stack, ISlotAction action) {
		this.stack = stack;
		this.action = action;
	}
}