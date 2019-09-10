package com.legendsofvaleros.modules.mount.api;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface IMount {
    String getId();

    Material getIcon();

    String getName();

    int getSpeedPercent();

    void hopOn(Player p);
}
