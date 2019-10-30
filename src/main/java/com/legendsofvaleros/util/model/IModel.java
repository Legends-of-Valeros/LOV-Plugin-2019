package com.legendsofvaleros.util.model;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import org.bukkit.Material;

public interface IModel {
    String getId();

    String getSlug();

    String getName();

    Material getMaterial();

    short getDamage();

    ItemBuilder toStack();
}
