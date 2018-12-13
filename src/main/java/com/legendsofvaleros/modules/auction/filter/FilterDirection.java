package com.legendsofvaleros.modules.auction.filter;

import org.bukkit.Material;

/**
 * Created by Crystall on 11/24/2018
 * Defines the direction to sort by
 */
public enum FilterDirection {
    ASCENDING("Ascending", "ASC", Material.GLASS_BOTTLE),
    DESCENDING("Descending", "DSC", Material.POTION);

    String name;
    String queryName;

    Material guiMaterial;

    FilterDirection(String name, String queryName, Material guiMaterial) {
        this.name = name;
        this.queryName = queryName;
        this.guiMaterial = guiMaterial;
    }

    public String getName() {
        return name;
    }

    public Material getGuiMaterial() {
        return guiMaterial;
    }

    public String getQueryName() {
        return queryName;
    }
}
