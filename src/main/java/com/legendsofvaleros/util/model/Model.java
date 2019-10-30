package com.legendsofvaleros.util.model;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.features.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class Model implements IModel {
    public static final Model NONE = new Model(Material.BEDROCK, (short)0);
    public static ItemStack EMPTY_SLOT = null;

    // Model values
    @SerializedName("_id")
    private String id;
    private String slug;

    private final String name;
    private final Material material;
    private final short damage;

    public Model(Material material, short damage) {
        this(null, "Generated", material, damage);
    }

    public Model(String id, String name, Material material, int damage) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.damage = (short)damage;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSlug() {
        return slug;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public short getDamage() {
        return damage;
    }

    @Override
    public ItemBuilder toStack() {
        return new ItemBuilder(material).setName(null).setDurability(damage).unbreakable().addFlag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
    }

    @Override
    public String toString() {
        return "Model(id=" + id + ", name=" + name + ", material=" + material + ", durability=" + damage + ")";
    }
}