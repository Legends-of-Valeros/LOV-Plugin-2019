package com.legendsofvaleros.modules.auction.filter;

import lombok.Getter;
import org.bukkit.Material;

/**
 * Created by Crystall on 11/24/2018
 * Defines the direction to sort by
 */
public enum FilterDirection {
    ASCENDING("Ascending", "ASC", Material.GLASS_BOTTLE),
    DESCENDING("Descending", "DSC", Material.POTION);

    @Getter
    String name;

    @Getter
    String queryName;

    @Getter
    Material guiMaterial;

    FilterDirection(String name, String queryName, Material guiMaterial) {
        this.name = name;
        this.queryName = queryName;
        this.guiMaterial = guiMaterial;
    }
}
