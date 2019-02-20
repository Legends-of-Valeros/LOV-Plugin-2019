package com.legendsofvaleros.modules.hearthstones.core;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import org.bukkit.Location;
import org.bukkit.World;

public class HomePoint {
	public CharacterId characterId;

	public String name;

	public World world;

	public int x;
	public int y;
	public int z;
	
	public HomePoint(CharacterId characterId, String name, Location location) {
		this.characterId = characterId;
		this.name = name;
		this.world = location.getWorld();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
	}

	public Location getLocation() {
		return new Location(world, x, y, z);
	}
}