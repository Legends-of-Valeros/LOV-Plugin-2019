package com.legendsofvaleros.modules.hearthstones.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.Location;

public class HomePoint {
	@SerializedName("_id")
	public PlayerCharacter pc;

	public String name;

	public Location location;
	
	public HomePoint(PlayerCharacter pc, String name, Location location) {
		this.pc = pc;
		this.name = name;
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}
}