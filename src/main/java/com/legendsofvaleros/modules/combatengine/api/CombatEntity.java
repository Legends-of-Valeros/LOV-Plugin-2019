package com.legendsofvaleros.modules.combatengine.api;

import java.util.UUID;

import org.bukkit.entity.LivingEntity;

/**
 * A collection of all of the combat-relevant information for an entity.
 */
public interface CombatEntity {

  /**
   * Gets the unique name of the entity this combat data is for.
   * 
   * @return The unique name of the entity.
   */
  UUID getUniqueId();

  /**
   * Gets the entity this data is for.
   * 
   * @return The entity whose combat data is tracked in this object. <code>null</code> if the entity
   *         is no longer in memory.
   */
  LivingEntity getLivingEntity();

  /**
   * Gets whether this object is still active and has any relevance to a real entity currently on
   * the server.
   * 
   * @return <code>true</code> if this is active and still functions, else <code>false</code>.
   */
  boolean isActive();

  /**
   * Gets whether this entity is a player.
   * <p>
   * Useful for distinguishing between players and non-players, even after the entity may have been
   * removed from memory.
   * 
   * @return <code>true</code> if this is a player, else <code>false</code>.
   */
  boolean isPlayer();

  /**
   * Gets the numerical stats for this entity.
   * 
   * @return This entity's stats.
   */
  EntityStats getStats();

  /**
   * Gets the toggleable status effects for this entity.
   * 
   * @return This entity's status effects.
   */
  EntityStatusEffects getStatusEffects();

  /**
   * Gets a collection of the threat that this entity feels towards enemies, if this entity is not a
   * player.
   * <p>
   * Threat defines which enemy an ai entity will target. The enemy with the highest threat is this
   * entity's current target.
   * 
   * @return The threat levels of this ai entity towards its enemies. <code>null</code> if this
   *         entity is human-controlled and has free will.
   */
  EntityThreat getThreat();

}
