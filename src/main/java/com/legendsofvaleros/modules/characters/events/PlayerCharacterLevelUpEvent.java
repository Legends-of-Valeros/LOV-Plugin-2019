package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.event.HandlerList;

/**
 * An event called when a player-character gets enough listener to level up.
 */
public class PlayerCharacterLevelUpEvent extends PlayerCharacterEvent {

  private static final HandlerList handlers = new HandlerList();

  private final int newLevel;

  /**
   * Class constructor.
   * 
   * @param playerCharacter The player character who is leveling up.
   * @param newLevel The level the player character has achieved. Cannot be negative.
   */
  public PlayerCharacterLevelUpEvent(PlayerCharacter playerCharacter, int newLevel) {
    super(playerCharacter);
    if (newLevel < 0) {
      throw new IllegalArgumentException("level cannot be negative");
    }
    this.newLevel = newLevel;
  }

  /**
   * Gets the level that the player-character is leveling up to.
   * 
   * @return The newly achieved level.
   */
  public int getNewLevel() {
    return newLevel;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
