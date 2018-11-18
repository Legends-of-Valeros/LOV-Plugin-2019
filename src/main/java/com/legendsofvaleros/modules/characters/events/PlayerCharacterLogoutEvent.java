package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.event.HandlerList;

/**
 * An event called when a player stops playing one of their characters.
 * <p>
 * Called either when a player logs out or they change to another character.
 * <p>
 * This event <b>is</b> called on shutdown for any logged-in player characters. Bukkit's default
 * behavior of disabling listeners on-disable is bypassed and listeners that rely on this should
 * still be called, but this should be carefully tested to ensure it works in a specific context.
 */
public class PlayerCharacterLogoutEvent extends PlayerCharacterEvent {

  private static final HandlerList handlers = new HandlerList();

  private boolean loggedOut;

  public PlayerCharacterLogoutEvent(PlayerCharacter playerCharacter, boolean loggedOutOfServer) {
    super(playerCharacter);
    this.loggedOut = loggedOutOfServer;
  }

  /**
   * Gets whether the player character is no longer being played because the player logged out of
   * the server altogether or not.
   * 
   * @return <code>true</code> if the player logged out of the Minecraft server, <code>false</code>
   *         if the player just switched to another character.
   */
  public boolean isServerLogout() {
    return loggedOut;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
