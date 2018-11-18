package com.legendsofvaleros.modules.characters.config;

import org.bukkit.Location;

/**
 * Configuration for setting certain locations that Characters uses on the server.
 */
public interface LocationConfig {

  /**
   * Gets the location that players are teleported to when they start creating a character.
   * 
   * @return The loading location.
   */
  Location getCreateLocation();

  /**
   * Gets the location that players are teleported to when they finish creating a character.
   * 
   * @return The starting location.
   */
  Location getStartLocation();

}
