package com.legendsofvaleros.modules.gear.item;

import com.legendsofvaleros.util.item.Model;

public interface IGear {
    /**
     * A unique identifier for the item.
     * @return
     */
    String getID();

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
}