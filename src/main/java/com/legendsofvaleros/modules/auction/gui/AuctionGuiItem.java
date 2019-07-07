package com.legendsofvaleros.modules.auction.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Crystall on 02/05/2019
 */
public enum AuctionGuiItem {
    PREVIOUS_PAGE("Previous Page", Material.PAPER, "Go to previous page"),
    NEXT_PAGE("Next Page", Material.PAPER, "Go to next page"),
    REFRESH("Refresh", Material.MUSIC_DISC_CAT, "Refresh all entries");

    public String title;
    public Material material;
    public String[] lore;


    AuctionGuiItem(String title, Material material, String... lore) {
        this.title = title;
        this.material = material;
        this.lore = lore;
    }

    /**
     * Returns the gui item with all its properties
     * @return
     */
    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(this.material);
        ItemMeta im = is.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(Arrays.toString(this.lore));
        im.setDisplayName(this.title);
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }
}
