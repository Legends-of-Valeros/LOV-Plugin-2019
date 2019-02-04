package com.legendsofvaleros.modules.npcs.core;

import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.Location;

public class NPCData {
	public String npcId;
	public String name;
	public String skin;
	public LOVTrait[] traits;
	
	public Location loc;
	
	public <T extends LOVTrait> T getTrait(Class<T> trait) {
		for(LOVTrait t : traits) {
			if(trait.isAssignableFrom(t.getClass()))
				return trait.cast(t);
		}
		
		return null;
	}
}
