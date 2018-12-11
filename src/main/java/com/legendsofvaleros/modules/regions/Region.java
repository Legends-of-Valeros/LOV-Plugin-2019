package com.legendsofvaleros.modules.regions;

import com.codingforcookies.doris.orm.annotation.Column;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public class Region {
	public String id;
	public World world;
	
	private final RegionBounds bounds;
	public RegionBounds getBounds() { return bounds; }

	public boolean allowAccess = false;
	public boolean allowHearthstone = true;
	public List<String> quests;
	
	public String msgEnter;
	public String msgExit;
	public String msgFailure = "You cannot go there, yet.";
	
	public Region(String id, World world, RegionBounds bounds) {
		this.id = id;
		this.world = world;
		this.bounds = bounds;
	}

	public boolean isInside(Location location) {
		return bounds.isInside(location);
	}
}