package com.legendsofvaleros.modules.gear.api;

import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.GearRarity;
import com.legendsofvaleros.modules.gear.core.GearType;
import com.legendsofvaleros.util.model.Model;
import org.bukkit.inventory.ItemStack;

public interface IGear {
    /**
     * A unique identifier for the item.
     * @return
     */
    String getId();

    String getSlug();

    /**
     * The current version of the item. If this is changes between
     * decoding, the item is completely regenerated.
     * @return
     */
    int getVersion();

    /**
     * The display name of the item.
     * @return
     */
    String getName();

    /**
     * The type of the item.
     * @return
     */
    GearType getType();

    /**
     * The ID of the model for the item.
     * @return
     */
    String getModelId();

    /**
     * A model object based on the model ID of the item.
     * @return
     */
    Model getModel();

    /**
     * The maximum amount of items that this can stack to.
     * TODO: Respect max stack size
     * @return
     */
    byte getMaxAmount();

    /**
     * The rarity of the item.
     * @return
     */
    GearRarity getRarityLevel();

    /**
     * Returns a seed for deterministic randomization based on the unique ID of this item.
     * @return
     */
    int getSeed();

    Gear.Instance newInstance();

    boolean isSimilar(ItemStack newArmorPiece);

    boolean isSimilar(Gear.Instance gearInstance);

    boolean isSimilar(IGear gear);
}