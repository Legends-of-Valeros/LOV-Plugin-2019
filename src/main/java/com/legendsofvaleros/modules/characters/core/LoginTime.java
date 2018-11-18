package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks when player-characters log in.
 */
public class LoginTime {

  private static Map<CharacterId, Long> loginTimes;

  // prevents accidental construction
  private LoginTime() {}

  static void onEnable() {
    loginTimes = new HashMap<>();
    Bukkit.getPluginManager().registerEvents(new LoginListener(), LegendsOfValeros.getInstance());
  }

  /**
   * Gets a millisecond timestamp of when a current player-character logged in.
   * 
   * @param currentPc The currently logged-in player character to get when they finished loading and
   *        successfully logged in.
   * @return A millisecond timestamp of when the player character last logged in (when they finished
   *         loading and were allowed to start playing).
   * @throws IllegalArgumentException On a <code>null</code> player-character or a player-character
   *         that is not currently logged in.
   */
  public static long getLastLogin(PlayerCharacter currentPc) throws IllegalArgumentException {
    if (currentPc == null || !currentPc.isCurrent()) {
      throw new IllegalArgumentException("player-character must be not null and logged in");
    }
    return loginTimes.get(currentPc.getUniqueCharacterId());
  }

  /**
   * Listens for player-character logins and logouts.
   */
  private static class LoginListener implements Listener {

    @EventHandler
    public void onPlayerCharacterFinishLoading(PlayerCharacterFinishLoadingEvent event) {
      loginTimes.put(event.getPlayerCharacter().getUniqueCharacterId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerCharacterLogout(PlayerCharacterLogoutEvent event) {
      loginTimes.remove(event.getPlayerCharacter().getUniqueCharacterId());
    }

  }

}
