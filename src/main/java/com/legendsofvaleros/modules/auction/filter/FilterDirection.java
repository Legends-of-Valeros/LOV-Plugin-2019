package com.legendsofvaleros.modules.auction.filter;

import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Crystall on 11/24/2018
 * Defines the direction to sort by
 */
public enum FilterDirection {
    ASCENDING("ascending", "ASC", Material.GLASS_BOTTLE),
    DESCENDING("descending", "DSC", Material.POTION);

    private String name;
    private String queryName;
    private Material guiMaterial;

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

    public String getDescription() {
        return "Filtering the values " + name;
    }

    /**
     * Returns the gui item with all its properties
     * @return
     */
    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(this.guiMaterial);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(StringUtils.capitalize(this.name));
        im.setLore(Arrays.asList(this.getDescription()));
        is.setItemMeta(im);
        return is;
    }
}
