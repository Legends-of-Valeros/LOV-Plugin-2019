package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.modules.levelarchetypes.api.LevelProvider;
import com.legendsofvaleros.modules.levelarchetypes.api.LevelProvider;
import com.legendsofvaleros.modules.levelarchetypes.api.LevelProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Provides the levels of players, based on their currently played character, if any.
 */
public class PlayerCharacterLevelProvider implements LevelProvider {

  @Override
  public int getLevel(LivingEntity entity) {
    if (entity == null || entity.getType() != EntityType.PLAYER) {
      return 0;
    }
    Player player = (Player) entity;
    if(!Characters.isPlayerCharacterLoaded(player)) return 0;
    return Characters.getPlayerCharacter(player).getExperience().getLevel();
  }

}
