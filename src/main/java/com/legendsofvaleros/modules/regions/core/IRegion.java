package com.legendsofvaleros.modules.regions.core;

import com.legendsofvaleros.modules.quests.api.IQuest;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public interface IRegion {
    String getId();

    boolean isInside(Location location);

    World getWorld();

    RegionBounds getBounds();

    boolean isAllowedByDefault();

    boolean areHearthstonesAllowed();

    List<IQuest> getQuestsTriggered();

    String getEnterMessage();

    String getExitMessage();

    String getErrorMessage();
}
