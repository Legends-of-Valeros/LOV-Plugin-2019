package com.legendsofvaleros.modules.npcs.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.api.ISkin;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.World;

public class NPCData implements INPC {
	@SerializedName("_id")
	private String id;
	private String slug;

	private String name;
	private ISkin skin;
	public LOVTrait[] traits;

	public World world;
	public int x, y, z;

    private transient Location loc;

    public String getId() {
    	return id;
	}

	public String getSlug() {
    	return slug;
	}

	@Override
	public String getName() {
    	return name;
	}

	public ISkin getSkin() {
    	return skin;
	}

	@Override
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

	@Override
    public String toString() {
        return "NPC(id=" + id + ", name=" + name + ", skin=" + skin + ", traits=" + (traits != null ? "Trait(length=" + traits.length + ")" : null) + ")";
    }
}
