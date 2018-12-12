package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.Location;

/**
 * An event called when an entity is damaged by true damage.
 * <p>
 * True damage ignores defensive stats and other modifiers, it cannot miss, and it cannot critical
 * hit.
 */
public class CombatEngineTrueDamageEvent extends CombatEngineDamageEvent {

  /**
   * Class constructor.
   * 
   * @param damaged The entity that is being damaged.
   * @param damager The entity that is causing the damage. Can be <code>null</code> if the cause is
   *        ambiguous or no entity caused it directly.
   * @param damageOrigin The location the damage is coming from. The entity will be knocked
   *        backwards from this location. Can be <code>null</code> for no knockback to take place.
   * @param damage The raw amount of damage being applied.
   * @throws IllegalArgumentException On a <code>null</code> damaged entity.
   */
  public CombatEngineTrueDamageEvent(CombatEntity damaged, CombatEntity damager,
      Location damageOrigin, double damage) throws IllegalArgumentException {
    // 1.0 because true damage ignores modifiers
    super(damaged, damager, damageOrigin, damage, false);
  }

}
