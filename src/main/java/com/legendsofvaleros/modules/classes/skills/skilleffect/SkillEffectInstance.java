package com.legendsofvaleros.modules.classes.skills.skilleffect;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * An instance of a skill/spell effect specific to an individual entity.
 */
public interface SkillEffectInstance {

  /**
   * Gets the effect this is an instance of.
   * 
   * @return The underlying effect this is a per-entity instance of.
   */
  SkillEffect<?> getEffect();

  /**
   * Gets the name of the affected entity.
   * <p>
   * Works even if the entity itself has left memory.
   * 
   * @return The name of the affected entity.
   */
  UUID getAffectedId();

  /**
   * Gets the affected entity, if they are still in memory.
   * 
   * @return The affected entity if it is still in memory. Else <code>null</code>.
   */
  LivingEntity getAffected();

  /**
   * Gets the name of the entity that caused this instance of the effect, if it was caused by a
   * specific entity.
   * <p>
   * Works even if the entity itself has left memory.
   * 
   * @return The name of the entity that caused this instance of the effect, if it was caused by a
   *         specific entity and that entity. Else <code>null</code>.
   */
  UUID getAppliedById();

  /**
   * Gets the unique name of the player-character that applied this instance of the effect, only if it
   * was caused by a player.
   * 
   * @return The name of the player-character that applied this effect, only if it was applied by a
   *         player-character. Else <code>null</code>.
   */
  CharacterId getAppliedByCharacterId();

  /**
   * Gets the entity that caused this instance of effect, if it was caused by a specific entity and
   * they are still in memory.
   * 
   * @return The entity that caused this instance of the effect, if it was caused by a specific
   *         entity <i>and></i> that entity is still in memory. Else <code>null</code>.
   */
  LivingEntity getAppliedBy();

  /**
   * Gets the level of this instance of an effect.
   * 
   * @return This effect instance's level.
   */
  int getLevel();

  /**
   * Gets how many milliseconds are remaining in this effect's lifetime.
   * 
   * @return How many milliseconds this will last until it expires.
   */
  long getRemainingDurationMillis();

  /**
   * Gets how many milliseconds have passed in this effect's lifetime.
   * 
   * @return How many milliseconds the affected entity has been affected by this effect (in effect,
   *         effectually. effect?).
   */
  long getElapsedDurationMillis();

}
