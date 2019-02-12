package com.legendsofvaleros.modules.zones.core;

import com.codingforcookies.ambience.Sound;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class Zone {
	public String id;
	public int y = 0;
	
	public String channel;
	public String name;
	public String subname;
	
	public MaterialWithData material;
	
	public boolean pvp;
	
	public Sound[] ambience;
	
	@SuppressWarnings("deprecation")
	public boolean isInZone(Location loc) {
		if(loc == null) return false;
		Block b = loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
		return b.getType() == material.type && b.getData() == material.data;
	}
}