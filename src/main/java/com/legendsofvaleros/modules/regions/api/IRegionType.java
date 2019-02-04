package com.legendsofvaleros.modules.regions.api;

import org.bukkit.entity.Player;

public interface IRegionType {
	void onEnter(String regionID, Player p);
	void onLeave(String regionID, Player p);
}