package com.legendsofvaleros.modules.npcs.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.api.ISkin;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

public class LOVNPC implements INPC {
	@SerializedName("_id")
	private String id;
	private String slug;

	private String name;
	private ISkin skin;
	public LOVTrait[] traits;

	public Location location;

    @Override
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

	@Override
	public ISkin getSkin() {
    	return skin;
	}

	@Override
    public Location getLocation() {
    	return this.location;
	}

	public void setLocation(NPC npc) {
		this.location = npc.getEntity().getLocation().getBlock().getLocation();
	}

	@Override
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
