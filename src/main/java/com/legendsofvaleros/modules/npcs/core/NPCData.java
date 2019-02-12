package com.legendsofvaleros.modules.npcs.core;

import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.World;

public class NPCData {
	public String id;
	public String name;
	public String skin;
	public LOVTrait[] traits;

	public World world;
	public int x, y, z;

    private transient Location loc;

    public Location getLocation() {
    	if(this.loc == null && world != null)
			this.loc = new Location(world, x, y, z);
    	return this.loc;
	}

	public void setLocation(NPC npc) {
		this.loc = npc.getEntity().getLocation().getBlock().getLocation();
		this.world = loc.getWorld();
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
	}

    public <T extends LOVTrait> T getTrait(Class<T> trait) {
		for(LOVTrait t : traits) {
			if(trait.isAssignableFrom(t.getClass()))
				return trait.cast(t);
		}
		
		return null;
	}
}
