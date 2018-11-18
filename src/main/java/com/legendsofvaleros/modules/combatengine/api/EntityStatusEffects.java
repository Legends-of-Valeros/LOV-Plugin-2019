package com.legendsofvaleros.modules.combatengine.api;

import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;

import java.util.Set;

/**
 * A collection of an entity's status effects.
 * <p>
 * Status effects are temporary special states an entity can be in that have no numerical value.
 * They simply toggle on/off.
 */
public interface EntityStatusEffects {

  /**
   * Gets the combat data these status effects are a part of.
   * 
   * @return The combat data that contains these status effects.
   */
  CombatEntity getCombatEntity();

  /**
   * Adds a status effect to the entity which will expire after a given period of time.
   * <p>
   * If the entity already has an effect of the same type active, whichever of the two effects'
   * durations is longer will be used.
   * 
   * @param type The type of status effect to apply.
   * @param durationTicks How long, in ticks (usually 1/20 of a second), the status effect should
   *        last.
   * @return <code>true</code> if this effect was successfully added. <code>false</code> if adding
   *         the effect failed or an existing effect was kept instead.
   */
  boolean addStatusEffect(StatusEffectType type, long durationTicks);

  /**
   * Removes a status effect from the entity.
   * 
   * @param type The type of effect to remove.
   * @return <code>true</code> if the entity had the effect and it was removed, <code>false</code>
   *         if they did not have the effect and nothing happened.
   */
  boolean removeStatusEffect(StatusEffectType type);

  /**
   * Gets whether the entity is currently under a type of status effect.
   * 
   * @param type The type of status effect to check for.
   * @return <code>true</code> if the entity currently has the effect, else <code>false</code>.
   */
  boolean hasStatusEffect(StatusEffectType type);

  /**
   * Gets when an entity's status effect will expire.
   * 
   * @param type The type of status effect to get the remaining duration of.
   * @return A millisecond timestamp of when the entity will no longer be affected by the given
   *         status effect (unless it is extended in the meantime). <code>0</code> if the entity
   *         does is not currently affected by the given status effect type.
   */
  long getStatusEffectExpiry(StatusEffectType type);

  /**
   * Gets all of the entity's currently active status effects.
   * 
   * @return A copy of the entity's current status effects.
   */
  Set<StatusEffectType> getActiveStatusEffects();

  /**
   * Gets whether this entity can use spells/skills.
   * 
   * @return <code>true</code> if the entity can use spells/skills, as far as CombatEngine is
   *         concerned. <code>false</code> if not, such as if the entity is stunned, silenced, etc.
   */
  boolean canUseSkills();

}
