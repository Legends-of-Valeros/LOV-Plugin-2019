package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.event.player.PlayerEvent;

/**
 * An event that primarily involves a player character.
 */
public abstract class PlayerCharacterEvent extends PlayerEvent {

  private PlayerCharacter playerCharacter;

  public PlayerCharacterEvent(PlayerCharacter playerCharacter) {
    super(playerCharacter.getPlayer());
    this.playerCharacter = playerCharacter;
  }

  /**
   * Gets the player character this event is for/focused around.
   * 
   * @return The relevant player character.
   */
  public PlayerCharacter getPlayerCharacter() {
    return playerCharacter;
  }

}
