package com.codingforcookies.robert.core;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Crystall on 02/05/2019
 * Enum that contains items used in a gui with custom display name and lore
 */
public enum GuiItem {
    PREVIOUS_PAGE("Previous Page", Material.PAPER, "Go to previous page"),
    NEXT_PAGE("Next Page", Material.PAPER, "Go to next page"),
    REFRESH("Refresh", Material.MUSIC_DISC_CAT, "Refresh all entries"),

    // Queue
    ARENA_QUEUE("Arena", Material.IRON_SWORD, "Fight against the best of the best"),
    BATTLEGROUND_QUEUE("Battleground", Material.GRASS_BLOCK, "Fight your battle as a team"),
    DUNGEONS_QUEUE("Dungeon", Material.IRON_BARS, "Are you worthy to fight the strongest creatures?"),

    // Arena
    ONE_VERSUS_ONE("1 vs 1 - Casual", Material.APPLE, "Casual arena"),
    TWO_VERSUS_TWO("2 vs 2 - Casual", Material.APPLE, "Casual arena"),

    RANKED_ONE_VERSUS_ONE("1 vs 1 - Ranked", Material.GOLDEN_APPLE, "Ranked arena"),
    RANKED_TWO_VERSUS_TWO("2 vs 2 - Ranked", Material.GOLDEN_APPLE, "Ranked arena"),

    //Battleground
    BG_TEAM_DEATHMATCH("Team Deathmatch", Material.STONE_BRICKS, "Fight until the death count limit is reached"),

    // Dungeon
    DUNGEON_BOSS_ONE("[COMING SOON]", Material.COMMAND_BLOCK, "Work in progress");

    public String title;
    public Material material;
    public String[] lore;


    GuiItem(String title, Material material, String... lore) {
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

        ArrayList<String> tempLore = new ArrayList<>();
        tempLore.add(Arrays.toString(this.lore));
        im.setLore(tempLore);

        im.setDisplayName(this.title);
        is.setItemMeta(im);

        return is;
    }
}
