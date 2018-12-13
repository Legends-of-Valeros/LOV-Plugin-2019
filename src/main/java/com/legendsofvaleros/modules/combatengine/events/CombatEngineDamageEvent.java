package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ModifiableDouble;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.Map;

/**
 * An event called when damage is dealt through the combat engine.
 */
public class CombatEngineDamageEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final CombatEntity damaged;
	private final CombatEntity attacker;
	private Location origin;

	private final boolean isCrit;

	private ModifiableDouble damage;
	private Map<String, ValueModifier> modifiers;

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
	 * @param isCrit <code>true</code> if this is a critical hit, else <code>false</code>.
	 * @throws IllegalArgumentException On a <code>null</code> damaged entity.
	 */
	protected CombatEngineDamageEvent(CombatEntity damaged, CombatEntity attacker,
			Location damageOrigin, double rawDamage, boolean isCrit)
					throws IllegalArgumentException {
		if (damaged == null) {
			throw new IllegalArgumentException("damaged entity cannot be null");
		}

		this.damaged = damaged;
		this.attacker = attacker;
		this.origin = damageOrigin;

		this.damage = new ModifiableDouble() {
			@Override protected double sanitizeValue(double sanitize) { return sanitize < 0 ? 0 : sanitize; }
			@Override protected void onChange(double newValue, double previousValue) { }
		};
		this.damage.flatEdit(rawDamage, false);

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

	public Map<String, ValueModifier> getModifiers() {
		return modifiers;
	}

	public ValueModifierBuilder newDamageModifierBuilder(String name) throws IllegalArgumentException {
		return new ValueModifierBuilder(damage, null) {
			@Override
			public ValueModifier build() {
				ValueModifier mod = super.build();

				if(modifiers == null)
					modifiers = new HashMap<>();

				modifiers.put(name, mod);

				return mod;
			}
		};
	}

	/**
	 * The final amount of damage, after it has been affected by defensive stats and other modifiers.
	 * 
	 * @return The final amount of damage that will the damaged entity will actually take.
	 */
	public double getFinalDamage() {
		return damage.getFinalValue();
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

	public double getBaseDamage() {
	    return this.damage.getBaseValue();
	}
}
