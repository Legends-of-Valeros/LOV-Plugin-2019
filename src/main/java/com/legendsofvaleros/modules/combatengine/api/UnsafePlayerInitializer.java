package com.legendsofvaleros.modules.combatengine.api;

import org.bukkit.entity.Player;

/**
 * Allows a client to define when and how player's combat data is initialized/invalidated.
 * <p>
 * <b>Do not use this unless you know exactly what you are doing.</b>
 */
public interface UnsafePlayerInitializer {

  /**
   * Triggers the creation of a combat entity for a player.
   * <p>
   * <b>Do not use this unless you know exactly what you are doing.</b>
   * <p>
   * Should be called before a player actually uses any of their combat stats, especially before
   * they are actually in combat.
   * <p>
   * Does nothing if the player already has a valid combat entity object. In order to create a new
   * combat data object for a player, first invalidate their previous one with
   * {@link #invalidateCombatEntity(Player)}.
   * 
   * @param player The player to create a combat entity for.
   */
  void createCombatEntity(Player player);

  /**
   * Invalidates a player's combat entity object.
   * <p>
   * <b>Do not use this unless you know exactly what you are doing.</b>
   * <p>
   * Should not be used anywhere near combat or in any situation where the player's combat data will
   * be needed before a new one is created for them.
   * <p>
   * Does nothing if the player does not have a combat data object.
   * 
   * @param player The player whose combat entity object to invalidate.
   */
  void invalidateCombatEntity(Player player);

}
