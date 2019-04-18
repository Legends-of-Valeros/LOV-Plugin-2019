package com.legendsofvaleros.modules.professions.gathering.mining;

import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.professions.ProfessionTier;
import com.legendsofvaleros.util.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Crystall on 02/13/2019
 */
public enum MiningTier implements ProfessionTier {
    TIER_1(15, 5, 120, Material.COAL_ORE, Material.COAL, ChatColor.BLACK, "COAL PLACEHOLDER", "COAL_ORE PLACEHOLDER", "", ""),
    TIER_2(20, 10, 120, Material.IRON_ORE, Material.IRON_INGOT, ChatColor.GRAY, "IRON_INGOT PLACEHOLDER", "IRON_ORE PLACEHOLDER", "", ""),
    TIER_3(30, 20, 300, Material.GOLD_ORE, Material.GOLD_INGOT, ChatColor.GOLD, "GOLD_INGOT PLACEHOLDER", "GOLD_ORE PLACEHOLDER", "", ""),
    TIER_4(110, 20, 300, Material.DIAMOND_ORE, Material.DIAMOND, ChatColor.DARK_AQUA, "DIAMOND PLACEHOLDER", "DIAMOND_ORE PLACEHOLDER", "", ""),
    TIER_5(180, 40, 600, Material.EMERALD_ORE, Material.EMERALD, ChatColor.GREEN, "EMERALD PLACEHOLDER", "EMERALD_ORE PLACEHOLDER", "", ""),
    TIER_6(230, 40, 800, Material.REDSTONE_ORE, Material.REDSTONE, ChatColor.RED, "REDSTONE PLACEHOLDER", "REDSTONE_ORE PLACEHOLDER", "", ""),
    TIER_7(280, 40, 2400, Material.LAPIS_ORE, Material.INK_SACK, ChatColor.BLUE, "LAPIS PLACEHOLDER", "LAPIS PLACEHOLDER", "LAPIS_ORE PLACEHOLDER", "LAPIS_ORE PLACEHOLDER"),
    TIER_8(280, 40, 2400, Material.QUARTZ_ORE, Material.QUARTZ, ChatColor.GOLD, "QUARTZ PLACEHOLDER", "QUARTZ_ORE PLACEHOLDER", "", "");

    private final int baseXP;
    private final int randXP;
    private final int respawnTime;
    private final Material oreType;
    private final Material ingotType;
    private final ChatColor color;
    private final String title;
    private final String description;
    private final String oreTitle;
    private final String oreDescription;

    MiningTier(int baseXP, int randXP, int respawnTime, Material oreType, Material ingotType,
               ChatColor color, String title, String description, String oreTitle, String oreDescription) {
        this.baseXP = baseXP;
        this.randXP = randXP;
        this.respawnTime = respawnTime;
        this.oreType = oreType;
        this.ingotType = ingotType;
        this.color = color;
        this.title = title;
        this.description = description;
        this.oreTitle = oreTitle;
        this.oreDescription = oreDescription;
    }

    /**
     * Create the ore item for this tier.
     */
    public ItemStack createOre() {
        ItemStack ore = new ItemStack(getOreType(), 1);
        ItemMeta im = ore.getItemMeta();
        im.setDisplayName(oreTitle);
        im.setLore(Collections.singletonList(getOreDescription()));
        ore.setItemMeta(im);
        return ore;
    }

    /**
     * Gets the ore name.
     * @return oreName
     */
    public String getOreName() {
        return TextUtils.capitalize(getOreType().name().split("_")[0]);
    }

    /**
     * Get the mining tier for the given tier id.
     * @param tier
     * @return miningTier
     */
    public static MiningTier getTier(int tier) {
        return values()[tier];
    }

    /**
     * Get the mining tier based on an ore type.
     * @param ore
     * @return tier
     */
    public static MiningTier getTier(Material ore) {
        for (MiningTier tier : values()) {
            if (tier.getOreType() == ore) {
                return tier;
            }
        }
        return null;
    }

    public static ArrayList<Material> getOreMaterials() {
        ArrayList<Material> ores = new ArrayList<>();
        for (MiningTier tier : values()) {
            ores.add(tier.getOreType());
        }
        return ores;
    }

    public String getDescription() {
        return description;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getBaseXP() {
        return baseXP;
    }

    public int getRandXP() {
        return randXP;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public Material getOreType() {
        return oreType;
    }

    public String getOreDescription() {
        return oreDescription;
    }

    public Material getIngotType() {
        return ingotType;
    }
}