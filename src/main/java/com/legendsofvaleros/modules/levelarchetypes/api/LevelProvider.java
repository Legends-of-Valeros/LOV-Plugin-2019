package com.legendsofvaleros.modules.levelarchetypes.api;

import org.bukkit.entity.LivingEntity;

/**
 * Provides level info about entities.
 */
public interface LevelProvider {

  /**
   * Gets the level of an entity.
   * 
   * @param entity The entity to get the level of.
   * @return The entity's level.
   */
  int getLevel(LivingEntity entity);

}
