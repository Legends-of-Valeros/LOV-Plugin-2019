package com.legendsofvaleros.modules.combatengine.events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

/**
 * An event called when damage is dealt through the combat engine.
 */
public class CombatEngineDamageEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final CombatEntity damaged;
	private final CombatEntity attacker;
	private Location origin;
	private double rawDamage;
	private double damageMultiplier;
	private double swingMultiplier = 1D;
	private final boolean isCrit;

	private boolean cancelled;

	/**
	 * Class constructor.
	 * 
	 * @param damaged The entity that is being damaged.
	 * @param attacker The entity that is causing the damage. Can be <code>null</code> if the cause is
	 *        ambiguous or no entity caused it directly.
	 * @param damageOrigin The location the damage is coming from. The entity will be knocked
	 *        backwards from this location. Can be <code>null</code> for no knockback to take place.
	 * @param rawDamage The raw amount of damage being applied.
	 * @param damageMultiplier The final result, as a multiplier for the raw damage amount, of the sum
	 *        of the damaged entity's resistances, defensive stats, and other modifiers.
	 * @param isCrit <code>true</code> if this is a critical hit, else <code>false</code>.
	 * @throws IllegalArgumentException On a <code>null</code> damaged entity.
	 */
	protected CombatEngineDamageEvent(CombatEntity damaged, CombatEntity attacker,
			Location damageOrigin, double rawDamage, double damageMultiplier, double swingMultiplier, boolean isCrit)
					throws IllegalArgumentException {
		if (damaged == null) {
			throw new IllegalArgumentException("damaged entity cannot be null");
		}

		this.damaged = damaged;
		this.attacker = attacker;
		this.origin = damageOrigin;
		this.rawDamage = rawDamage;
		this.damageMultiplier = damageMultiplier;
		this.swingMultiplier = swingMultiplier;
		this.isCrit = isCrit;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	/**
	 * Gets the entity that was damaged.
	 * 
	 * @return The damaged entity.
	 */
	public CombatEntity getDamaged() {
		return damaged;
	}

	/**
	 * The entity that caused the damage, if any.
	 * 
	 * @return The damaging entity. <code>null</code> if the damage's cause was ambiguous or not
	 *         caused by another entity.
	 */
	public CombatEntity getAttacker() {
		return attacker;
	}

	/**
	 * Gets the location this damage is emanating from.
	 * <p>
	 * The damaged entity will be knocked backwards from this location upon taking damage.
	 * 
	 * @return The origin point of the damage. <code>null</code> if there is no defined origin and the
	 *         entity will not be knocked back upon receiving the damage.
	 */
	public Location getDamageOrigin() {
		return origin;
	}

	/**
	 * Gets the raw amount of damage that was done, before any modifiers or defensive stats are
	 * applied.
	 * 
	 * @return The raw, unprocessed amount of damage.
	 */
	public double getRawDamage() {
		return rawDamage;
	}

	/**
	 * @return The damage multiplier.
	 */
	public double getDamageMultiplier() {
		return damageMultiplier;
	}

	public double getSwingMultiplier() {
		return swingMultiplier;
	}

	public void setSwingMultiplier(double swingMultiplier) {
		this.swingMultiplier = swingMultiplier;
	}

	/**
	 * The final amount of damage, after it has been affected by defensive stats and other modifiers.
	 * 
	 * @return The final amount of damage that will the damaged entity will actually take.
	 */
	public double getFinalDamage() {
		return rawDamage * swingMultiplier * damageMultiplier;
	}

	/**
	 * Gets whether the damage represents a critical hit.
	 * <p>
	 * Some attacks have a random percentage chance to "crit", which increases the damage of that
	 * instance of damage.
	 * 
	 * @return <code>true</code> if the damage is a critical hit, else <code>false</code>.
	 */
	public boolean isCriticalHit() {
		return isCrit;
	}

	/**
	 * Sets the location this damage is emanating from.
	 * <p>
	 * The damaged entity will be knocked backwards from this location upon taking damage.
	 * 
	 * @param origin The origin point of the damage. <code>null</code> for the entity not to be
	 *        knocked back upon receiving the damage.
	 */
	public void setDamageOrigin(Location origin) {
		this.origin = origin;
	}

	/**
	 * Sets the raw amount of damage done.
	 * <p>
	 * To set final damage to an exact amount, cause true damage rather than physical or spell damage.
	 * True damage is not affected by multipliers.
	 * 
	 * @param damage The damage that should be edited by modifiers and then applied.
	 */
	public void setRawDamage(double damage) {
		this.rawDamage = damage;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
