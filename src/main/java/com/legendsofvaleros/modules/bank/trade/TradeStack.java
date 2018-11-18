package com.legendsofvaleros.modules.bank.trade;

import org.bukkit.inventory.ItemStack;

public class TradeStack {
	ItemStack item;
	int cost;
	
	public TradeStack(ItemStack item, int cost) {
		this.item = item;
		this.cost = cost;
		
		item.setAmount(1);
	}
}