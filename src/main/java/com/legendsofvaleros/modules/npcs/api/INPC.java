package com.legendsofvaleros.modules.npcs.api;

import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.Location;

public interface INPC {
    String getId();

    String getName();

    ISkin getSkin();

    Location getLocation();

    <T extends LOVTrait> T getTrait(Class<T> trait);
}