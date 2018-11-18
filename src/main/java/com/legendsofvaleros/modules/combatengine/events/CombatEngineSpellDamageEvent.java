package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import org.bukkit.Location;

/**
 * An event called when an entity is damaged by a spell.
 */
public class CombatEngineSpellDamageEvent extends CombatEngineDamageEvent {

  private final SpellType type;

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
   * @param spellType The type of the spell that is causing the damage.
   * @throws IllegalArgumentException On a <code>null</code> damaged entity.
   */
  public CombatEngineSpellDamageEvent(CombatEntity damaged, CombatEntity damager,
      Location damageOrigin, double rawDamage, double damageMultiplier, boolean isCrit,
      SpellType spellType) throws IllegalArgumentException {
    super(damaged, damager, damageOrigin, rawDamage, damageMultiplier, 1D, isCrit);
    if (spellType == null) {
      type = SpellType.OTHER;
    } else {
      type = spellType;
    }
  }

  /**
   * Gets the type of spell this damage is being caused by.
   * 
   * @return The damaging spell's type.
   */
  public SpellType getSpellType() {
    return type;
  }

}
