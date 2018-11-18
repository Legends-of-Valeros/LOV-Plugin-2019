package com.legendsofvaleros.modules.combatengine.ui;

import org.bukkit.entity.Player;

/**
 * Manages displaying combat information to players.
 */
public interface CombatEngineUiManager {

  /**
   * Gets the CombatEngine user interface object for a player, if one exists.
   * 
   * @param player The player to get a user interface for.
   * @return The player's user interface to inform of changes in their combat data.
   *         <code>null</code> if none was found for the given player.
   */
  PlayerCombatInterface getPlayerInterface(Player player);

}
