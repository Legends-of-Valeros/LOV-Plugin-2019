package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.statuseffects.*;

/**
 * Status effects that are simply on/off with a limited duration.
 * <p>
 * These are status effects that do not have a numerical value, but are centrally tracked to avoid
 * undesirable behaviors with overlapping effects of the same type.
 */
public enum StatusEffectType {
	INVINCIBILITY("Invincibility", "Invincible", "Applies invincibility effect to entity.", true, false, false),
	
	INVISIBILITY("Invisibility", "Invisible", "Applies invisibility effect to entity.", true, false, true),
	
	/**
	 * Prevents movement, interaction, skills, and attacks.
	 */
	CONFUSION("Confusion", "Confused", "Applies confusion effect to player.", false, true, true),

	/**
	 * Prevents movement, interaction, skills, and attacks.
	 */
	STUN("Stun", "Stunned", "Prevents movement, interaction, skills, and attacks.", false, true, true),

	/**
	 * Gives the entity night vision
	 */
	NIGHT_VISION("Night Vision", "Night Vision", "Supplies night vision to the entity.", true, false, false),

	/**
	 * Prevents casting spells or using skills.
	 */
	SILENCE("Silence", "Silenced", "Prevents casting spells or using skills.", false, true, false),

	/**
	 * Transformed into a harmless animal. Reduces speed and blocks skills and attacks.
	 */
	POLYMORPH("Polymorph", "Polymorphed",
			"Transformed into a harmless animal. Reduces speed and blocks skills and attacks.", false,
			true, true),

	/**
	 * Blocks vision and greatly reduces accuracy.
	 */
	BLINDNESS("Blindness", "Blinded", "Blocks vision and greatly reduces accuracy.", false, false, true);

	private final String uiName;
	private final String adj;
	private final String desc;
	private final boolean isGood;
	private final boolean stopsSkills;
	private final boolean removeOnDamage;

	StatusEffectType(String uiName, String adj, String desc, boolean isGood, boolean stopsSkills,
                     boolean removeOnDamage) {
		this.uiName = uiName;
		this.adj = adj;
		this.desc = desc;
		this.isGood = isGood;
		this.stopsSkills = stopsSkills;
		this.removeOnDamage = removeOnDamage;
	}
	
	/**
	 * Returns true if the status effect is generally recognized as a good status effect.
	 */
	public boolean isGood() {
		return isGood;
	}

	/**
	 * Gets a user-friendly name for describing this effect in user interfaces.
	 * <p>
	 * For example, <code>STUN</code> might be <code>"Stun"</code>.
	 * 
	 * @return A user-friendly string name for this effect.
	 */
	public String getUserFriendlyName() {
		return uiName;
	}

	/**
	 * Gets a user-friendly adjective that can be used to describe an entity that has this status
	 * effect.
	 * <p>
	 * For example, <code>STUN</code> might be <code>"Stunned"</code>.
	 * 
	 * @return A user-friendly adjective for entities that have this effect.
	 */
	public String getUserFriendlyAdjective() {
		return adj;
	}

	/**
	 * Gets a user-friendly description that can be used to describe what this status effect does to
	 * an entity.
	 * <p>
	 * For example, <code>STUN</code> might return
	 * <code>"Prevents movement, interaction, and attacks."</code>
	 * 
	 * @return A user-friendly description of this effect.
	 */
	public String getUserFriendlyDescription() {
		return desc;
	}

	/**
	 * Gets whether this status effect stops entities from using skills and spells.
	 * 
	 * @return <code>true</code> if this effect stops entities from using skills and spells for its
	 *         duration.
	 */
	public boolean blocksSkillsAndSpells() {
		return stopsSkills;
	}

	/**
	 * Gets whether this status effect type is removed when an entity that has it takes damage.
	 * 
	 * @return <code>true</code> if the status effect should be removed when an entity that has it
	 *         takes damage, else <code>false</code>.
	 */
	public boolean isRemovedOnDamage() {
		return removeOnDamage;
	}

	/**
	 * Applies this status effect to an entity.
	 * 
	 * @param entity The entity to apply the status effect to.
	 */
	void apply(CombatEntity entity) {
		switch (this) {
		
		case INVINCIBILITY:
			Invincibility.apply(entity);
			break;

		case INVISIBILITY:
			Invisibility.apply(entity);
			break;

		case CONFUSION:
			Confusion.apply(entity);
			break;

		case STUN:
			Stun.apply(entity);
			break;

		case NIGHT_VISION:
			NightVision.apply(entity);
			break;

		case SILENCE:
			// TODO is there anything we can actually do with a Silence class, or is it just a matter of
			// skill plugins polling CombatEngine to see if the player casting a spell is silenced?
			// Whether a player has it is tracked within CombinedCombatEntity, so it does not even seem
			// necessary to track who is silenced here. These classes are mostly to implement the
			// behavior of the status effects, and it seems like there is no actual behavior for silence
			// that can be defined within CombatEngine.
			break;

		case POLYMORPH:
			Polymorph.apply(entity);
			break;

		case BLINDNESS:
			Blindness.apply(entity);
			break;

		default:
			break;
		}
	}

	/**
	 * Removes this status effect from an entity.
	 * 
	 * @param entity The entity to remove this status effect from.
	 */
	void remove(CombatEntity entity) {
		switch (this) {

		case INVINCIBILITY:
			Invincibility.remove(entity);
			break;
		
		case INVISIBILITY:
			Invisibility.remove(entity);
			break;

		case CONFUSION:
			Confusion.remove(entity);
			break;

		case STUN:
			Stun.remove(entity);
			break;

		case NIGHT_VISION:
			NightVision.remove(entity);
			break;

		case SILENCE:
			break;

		case POLYMORPH:
			Polymorph.remove(entity);
			break;

		case BLINDNESS:
			Blindness.remove(entity);
			break;

		default:
			break;
		}
	}

	/**
	 * Different reasons for a status effect being removed.
	 */
	public enum RemovalReason {

		/**
		 * The status effect was interrupted or manually removed.
		 */
		INTERRUPTED(),

		/**
		 * The status effect reached the end of its lifetime and expired naturally.
		 */
		EXPIRED
	}

}
