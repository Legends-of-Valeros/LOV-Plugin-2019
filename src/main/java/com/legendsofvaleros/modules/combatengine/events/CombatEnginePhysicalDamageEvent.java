package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import org.bukkit.Location;

/**
 * An event called when an entity is damaged by physical attacks.
 */
public class CombatEnginePhysicalDamageEvent extends CombatEngineDamageEvent {

  private final PhysicalType type;

  /**
   * Class constructor.
   * 
   * @param damaged The entity that is being damaged.
   * @param damager The entity that is causing the damage. Can be <code>null</code> if the cause is
   *        ambiguous or no entity caused it directly.
   * @param damageOrigin The location the damage is coming from. The entity will be knocked
   *        backwards from this location. Can be <code>null</code> for no knockback to take place.
   * @param rawDamage The raw amount of damage being applied.
   * @param damageMultiplier The final result, as a multiplier for the raw damage amount, of the sum
   *        of the damaged entity's resistances, defensive stats, and other modifiers.
   * @param isCrit <code>true</code> if this is a critical hit, else <code>false</code>.
   * @param physicalType The type of physical damage being dealt.
   * @throws IllegalArgumentException On a <code>null</code> damaged entity.
   */
  public CombatEnginePhysicalDamageEvent(CombatEntity damaged, CombatEntity damager,
      Location damageOrigin, double rawDamage, double damageMultiplier, double swingCooldown, boolean isCrit,
      PhysicalType physicalType) throws IllegalArgumentException {
    super(damaged, damager, damageOrigin, rawDamage, damageMultiplier, swingCooldown, isCrit);
    if (physicalType == null) {
      this.type = PhysicalType.OTHER;
    } else {
      this.type = physicalType;
    }
  }

  /**
   * Gets the type of physical attack this damage is being caused by.
   * 
   * @return The damaging physical attack's type.
   */
  public PhysicalType getPhysicalAttackType() {
    return type;
  }

}
