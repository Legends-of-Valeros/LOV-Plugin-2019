package com.legendsofvaleros.modules.classes.stats;

/**
 * Stats whose effects are dependent on a player-character's class.
 */
public enum AbilityStat {

    STRENGTH("Strength", "STR"),
    STAMINA("Stamina", "STA"),
    AGILITY("Agility", "AGI"),
    ENDURANCE("Endurance", "END"),
    INTELLIGENCE("Intelligence", "INT");

    private final String uiName;
    private final String shortName;

    AbilityStat(String uiName, String shortName) {
        this.uiName = uiName;
        this.shortName = shortName;
    }

    /**
     * Sanitizes a theoretical value for this class stat to conform to its possible values.
     * @param value The value to sanitize.
     * @return A sane version of the value for this class stat.
     */
    public double sanitizeValue(double value) {
        if (value < 0) {
            return 0;
        }
        return value;
    }

    /**
     * Gets a user-friendly name of this class stat.
     * @return A user friendly name for this that can be used in user interfaces.
     */
    public String getUserFriendlyName() {
        return uiName;
    }

    /**
     * Gets the short name of this class stat.
     * @return A short name for this that can be used in user interfaces.
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Takes the value of this class stat and formats it in the best way for use in user displays.
     * @param value The value to make into a user-friendly string.
     * @return A user-friendly version of the given value for a class stat.
     */
    public String formatForUserInterface(double value) {
        return String.valueOf(Math.round(value));
    }
}
