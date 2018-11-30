package com.legendsofvaleros.modules.auction.filter;

import lombok.Getter;
import org.bukkit.Material;

/**
 * Created by Crystall on 11/05/2018
 * Types to filter for
 */
public enum FilterType {
    PRICE("price", "price", Material.GOLD_NUGGET),
    CREATED_AT("created_at", "created_at", Material.PAPER),
    REMAINING_TIME("valid_until", "valid_until", Material.WATCH),
    ITEM_ID("item_id", "item_id", Material.IRON_NUGGET);

    @Getter
    String name;

    @Getter
    String queryName;

    @Getter
    Material guiMaterial;

    FilterType(String name, String queryName, Material guiMaterial) {
        this.name = name;
        this.queryName = queryName;
        this.guiMaterial = guiMaterial;
    }


}
