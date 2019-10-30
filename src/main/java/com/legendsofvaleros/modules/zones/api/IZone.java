package com.legendsofvaleros.modules.zones.api;

import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.Location;

import java.util.Optional;

public interface IZone {
    String getId();

    String getName();

    boolean isInside(Location loc);

    Optional<Zone.Section> getSection(Location loc);
}
