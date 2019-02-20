package com.legendsofvaleros.modules.professions.mining;

import com.legendsofvaleros.modules.professions.ProfessionTier;
import com.legendsofvaleros.util.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Crystall on 02/13/2019
 */
public enum MiningTier implements ProfessionTier {
    TIER_1(1, 90, 35, 120, Material.COAL_ORE, Material.COAL, ChatColor.BLACK, "", ""),
    TIER_2(1, 275, 35, 300, Material.EMERALD_ORE, Material.COAL, ChatColor.GREEN, "", ""),
    TIER_3(4, 460, 80, 600, Material.IRON_ORE, Material.COAL, ChatColor.WHITE, "", ""),
    TIER_4(4, 820, 40, 1200, Material.DIAMOND_ORE, Material.COAL, ChatColor.DARK_AQUA, "", ""),
    TIER_5(5, 1025, 55, 2400, Material.GOLD_ORE, Material.COAL, ChatColor.GOLD, "", ""),
    TIER_6(6, 1025, 55, 2400, Material.GOLD_ORE, Material.COAL, ChatColor.GOLD, "", ""),
    TIER_7(7, 1025, 55, 2400, Material.GOLD_ORE, Material.COAL, ChatColor.GOLD, "", ""),
    TIER_8(8, 1025, 55, 2400, Material.GOLD_ORE, Material.COAL, ChatColor.GOLD, "", "");

    private final int hardness;
    private final int baseXP;
    private final int randXP;
    private final int respawnTime;
    private final Material oreType;
    private final Material ingotType;
    private final ChatColor color;
    private final String description;
    private final String oreDescription;

    MiningTier(int hardness, int baseXP, int randXP, int respawnTime, Material oreType, Material ingotType,
               ChatColor color, String description, String oreDescription) {
        this.hardness = hardness;
        this.baseXP = baseXP;
        this.randXP = randXP;
        this.respawnTime = respawnTime;
        this.oreType = oreType;
        this.ingotType = ingotType;
        this.color = color;
        this.description = description;
        this.oreDescription = oreDescription;
    }

    /**
     * Create the ore item for this tier.
     */
    public ItemStack createOre() {
        return null;
    }

    /**
     * Gets the ore name.
     *
     * @return oreName
     */
    public String getOreName() {
        return TextUtils.capitalize(getOreType().name().split("_")[0]);
    }

    /**
     * Get the mining tier for the given tier id.
     *
     * @param tier
     * @return miningTier
     */
    public static MiningTier getTier(int tier) {
        return values()[tier - 1];
    }

    /**
     * Get the mining tier based on an ore type.
     *
     * @param ore
     * @return tier
     */
    public static MiningTier getTier(Material ore) {
        for (MiningTier tier : values())
            if (tier.getOreType() == ore)
                return tier;
        return null;
    }

    /**
     * Get an array of the ore related to mining.
     *
     * @return ore.
     */
    public static Material[] getOre() {
        Material[] types = new Material[values().length];
        for (int i = 0; i < types.length; i++)
            types[i] = values()[i].getOreType();
        return types;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getBaseXP() {
        return baseXP;
    }

    public int getHardness() {
        return hardness;
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

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public int getMinLevel() {
        return 0;
    }

    public Material getIngotType() {
        return ingotType;
    }
}