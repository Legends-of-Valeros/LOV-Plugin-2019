package com.legendsofvaleros.modules.regions.core;

import org.bukkit.Location;

public class RegionBounds {
	private int startX, startY, startZ;
	private int endX, endY, endZ;

	public int getStartX() { return startX; }
	public int getStartY() { return startY; }
	public int getStartZ() { return startZ; }

	public int getEndX() { return endX; }
	public int getEndY() { return endY; }
	public int getEndZ() { return endZ; }
	
	public RegionBounds() { }
	
	public RegionBounds setBounds(Location loc1, Location loc2) {
		startX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		startY = Math.min(loc1.getBlockY(), loc2.getBlockY());
		startZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		endX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		endY = Math.max(loc1.getBlockY(), loc2.getBlockY());
		endZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		return this;
	}
	
	public RegionBounds setBounds(int startX, int startY, int startZ, int endX, int endY, int endZ) {
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;

		this.endX = endX;
		this.endY = endY;
		this.endZ = endZ;
		
		return this;
	}
	
	public boolean isInside(Location location) {
		if(location == null)
			return false;
		return (location.getBlockX() >= startX && location.getBlockX() <= endX)
				&& (location.getBlockY() >= startY && location.getBlockY() <= endY)
				&& (location.getBlockZ() >= startZ && location.getBlockZ() <= endZ);
	}

    public int[] getCenter() {
		return new int[] { (startX + endX) / 2,
							(startY + endY) / 2,
							(startZ + endZ) / 2 };
    }
}