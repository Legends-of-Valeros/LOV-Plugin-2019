package com.legendsofvaleros.modules.professions;

/**
 * Created by Crystall on 02/12/2019
 */
public interface ProfessionTier {

    /**
     * return description
     *
     * @return
     */
    String getDescription();

    /**
     * Returns the id of the enum value
     *
     * @return
     */
    default int getId() {
        return ((Enum) this).ordinal() + 1;
    }

    /**
     * Get the minimum level for this tier.
     *
     * @return minLevel
     */
    default int getMinLevel() {
        return Math.max(getId() * 10, 1);
    }
}
