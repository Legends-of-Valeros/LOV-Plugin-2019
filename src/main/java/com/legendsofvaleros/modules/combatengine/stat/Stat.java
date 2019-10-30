package com.legendsofvaleros.modules.combatengine.stat;

/**
 * Stats that affect entities in combat.
 */
public enum Stat {
	// For non-developers: the capitalized names with "_" in them are the "enum names" (ex:
	// MAX_HEALTH). These are what CombatEngine uses to identify these stats. Use these in
	// configuration files in when referring to these stats.

	// Maximum health, mana, and energy levels
	MAX_HEALTH(Category.BASIC, "Maximum Health", "Max HP"),
	MAX_MANA(Category.BASIC, "Maximum Mana", "Max Mana"),
	MAX_ENERGY(Category.BASIC, "Maximum Energy", "Max Energy"),

	// Health, mana, and energy regeneration rates
	HEALTH_REGEN(Category.BASIC, "Health Regeneration", "HP Regen"),
	MANA_REGEN(Category.BASIC, "Mana Regeneration", "Mana Regen"),
	ENERGY_REGEN(Category.BASIC, "Energy Regeneration", "Energy Regen"),

	// Movement/running speed
	SPEED(Category.COMBAT, "Speed", "Speed"),

	// Physical and magical attack damage boosts
	PHYSICAL_ATTACK(Category.COMBAT, "Physical Attack", "Physical ATK"),
	MAGIC_ATTACK(Category.COMBAT, "Magic Power", "Magic Power"),

	// Resistance to damage from armor
	ARMOR(Category.COMBAT, "Armor", "Armor"),

	// Resistance to different types of spell damage
	FIRE_RESISTANCE(Category.COMBAT, "Fire Resistance", "Fire Resist"),
	ICE_RESISTANCE(Category.COMBAT, "Ice Resistance", "Ice Resist"),

	// Chance to hit with an attack that can miss
	HIT_CHANCE(Category.COMBAT, "Accuracy", "Hit %"),

	// Chance to dodge an attack that can miss
	DODGE_CHANCE(Category.COMBAT, "Dodge Chance", "Dodge %"),

	// Chance to get a critical hit with an attack that can crit
	CRIT_CHANCE(Category.COMBAT, "Critical Hit Chance", "Crit %");

	private Category category;
	private String uiName;
	private String shortName;

	Stat(Category category, String uiName, String shortName) {
		this.category = category;
		this.uiName = uiName;
		this.shortName = shortName;
	}

	/**
	 * Sanitizes a theoretical value for this stat to conform to its possible values.
	 * 
	 * @param value The value to sanitize.
	 * @return A sane version of the value for this stat.
	 */
	public double sanitizeValue(double value) {
		switch (this) {
		case MAX_HEALTH:
		case MAX_MANA:
		case MAX_ENERGY:
			if (value < 1) {
				return 1;
			}
			return value;

		case SPEED:
			if (value < 0) {
				return 0;
			}
			return value;

		case HEALTH_REGEN:
		case MANA_REGEN:
		case ENERGY_REGEN:
			return value;

		case HIT_CHANCE:
		case DODGE_CHANCE:
		case CRIT_CHANCE:
			if (value < 0) {
				return 0;
			}
			return value;

		default:
			return value;
		}
	}
	
	public Category getCategory() {
		return category;
	}

	/**
	 * Gets a user-friendly name for this stat that can be used in user interfaces.
	 * 
	 * @return The user-friendly name of this stat.
	 */
	public String getUserFriendlyName() {
		return uiName;
	}

	/**
	 * Gets the short name of this stat.
	 * 
	 * @return A short name for this that can be used in user interfaces.
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Takes the value of this stat and formats it in the best way for use in user displays, specific
	 * to how this stat is used and thought of.
	 * <p>
	 * For example, some stats should be displayed as integers with any trailing decimals rounded to
	 * the nearest whole number. Others might be best displayed as a percentage rounded to the nearest
	 * hundredth.
	 * 
	 * @param value The value to make into a user-friendly string.
	 * @return A user-friendly version of the given value for a stat.
	 */
	public String formatForUserInterface(double value) {
		switch (this) {

		case HIT_CHANCE:
		case DODGE_CHANCE:
		case CRIT_CHANCE:
			return Math.round(value) + "%";

		default:
			return String.valueOf(Math.round(value));
		}
	}
	
	public enum Category {
		BASIC("Basic"),
		COMBAT("Combat");

		private String uiName;
		
		Category(String uiName) {
			this.uiName = uiName;
		}

		/**
		 * Gets a user-friendly name for this category that can be used in user interfaces.
		 * 
		 * @return The user-friendly name of this category.
		 */
		public String getUserFriendlyName() {
			return uiName;
		}
	}
}
