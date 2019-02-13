package com.legendsofvaleros.modules.auction.filter;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Crystall on 11/05/2018
 * Types to filter for
 */
public enum FilterType {
    PRICE("price", "price", Material.GOLD_NUGGET),
    CREATED_AT("created at", "created_at", Material.PAPER),
    REMAINING_TIME("valid until", "valid_until", Material.WATCH),
    ITEM_ID("item id", "item_id", Material.IRON_NUGGET);

    private String name;
    private String queryName;
    private Material guiMaterial;

    FilterType(String name, String queryName, Material guiMaterial) {
        this.name = name;
        this.queryName = queryName;
        this.guiMaterial = guiMaterial;
    }

    public String getName() {
        return name;
    }

    public String getQueryName() {
        return queryName;
    }

    public Material getGuiMaterial() {
        return guiMaterial;
    }

    public String getDescription() {
        return "Filter by " + name;
    }

    /**
     * Returns the gui item with all its properties
     *
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
