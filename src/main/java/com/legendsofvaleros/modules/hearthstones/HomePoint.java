package com.legendsofvaleros.modules.hearthstones;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Table(name = "player_hearthstones")
public class HomePoint {
	@Column(primary = true, name = "character_id", length = 39)
	public CharacterId characterId;

	@Column(name = "hearthstone_name")
	public String innName;

	@Column(name = "hearthstone_world")
	public String world;

	@Column(name = "hearthstone_x")
	public int x;

	@Column(name = "hearthstone_y")
	public int y;

	@Column(name = "hearthstone_z")
	public int z;
	
	public HomePoint(CharacterId characterId, String innName, Location location) {
		this.characterId = characterId;
		this.innName = innName;
		this.world = location.getWorld().getName();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
}