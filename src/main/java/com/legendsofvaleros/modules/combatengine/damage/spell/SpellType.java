package com.legendsofvaleros.modules.combatengine.damage.spell;

import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.Sound;

/**
 * Possible types of spells.
 */
public enum SpellType {

	OTHER(null),
	FIRE(null, Stat.FIRE_RESISTANCE),
	ICE(null, Stat.ICE_RESISTANCE),
	POISON(null);

	private Sound hitSound;
	private Stat[] resistances;

	SpellType(Sound hitSound, Stat... applicableResistances) {
		this.hitSound = hitSound;
		if(applicableResistances == null || applicableResistances.length == 0)
			this.resistances = new Stat[] { };
		else
			this.resistances = applicableResistances;
	}

	public Sound getHitSound() {
		return hitSound;
	}

	/**
	 * Gets a copy of the types of resistances that this spell should be affected by.
	 * 
	 * @return The resistances that affect this type of spell.
	 */
	public Stat[] getApplicableResistances() {
		return resistances.clone();
	}

}
