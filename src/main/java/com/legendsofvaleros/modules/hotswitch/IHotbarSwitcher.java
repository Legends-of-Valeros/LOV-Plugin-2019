package com.legendsofvaleros.modules.hotswitch;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface IHotbarSwitcher {
	/**
	 * Returns the new index of the player's hotbar
	 */
	int onSwitch(Player p, int currentHotbar);
}