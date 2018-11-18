package com.legendsofvaleros.modules.characters.cooldown;

import com.legendsofvaleros.modules.characters.api.Cooldowns.CooldownType;

import java.util.LinkedList;
import java.util.List;

/**
 * Tracks data that can be used to calculate the expirations for an individual player-character's
 * cooldowns.
 */
public class RemainingDurationCalculator {

  private Long playerLogout;

  private List<CharacterSession> sessions;
  private CharacterSession current;

  /**
   * Class constructor that should be invoked as soon as a players' character and cooldown data is
   * loaded.
   */
  public RemainingDurationCalculator() {
    sessions = new LinkedList<>();
  }

  /**
   * Gets how many more milliseconds need to tick down before a cooldown expires.
   * 
   * @param type The type of cooldown.
   * @param timeWhenStartedLocally When the cooldown was loaded into memory or created in memory.
   * @param durationMillis The remaining duration of the cooldown, <b>from when it was loaded into
   *        or created in memory</b>, in milliseconds.
   * @return The remaining number of milliseconds.
   */
  public long getRemainingMillis(CooldownType type, long timeWhenStartedLocally, long durationMillis) {
    long timePassed = 0;

    switch (type) {
      case CALENDAR_TIME:
        timePassed = System.currentTimeMillis() - timeWhenStartedLocally;
        break;

      case CHARACTER_PLAY_TIME:
        timePassed = getCharacterPlayTime(timeWhenStartedLocally);
        break;

      case PLAYER_PLAY_TIME:
        if (playerLogout != null) {
          // player has logged out, and their play-time cooldown is no longer ticking down
          timePassed = playerLogout - timeWhenStartedLocally;
        } else {
          timePassed = System.currentTimeMillis() - timeWhenStartedLocally;
        }
        break;
    }

    long ret = durationMillis - timePassed;
    if (ret < 0) {
      return 0;
    }
    return ret;
  }

  /**
   * Informs this when the player-character starts being played.
   * <p>
   * It is essential to fully report character logins for accurate calculations.
   */
  public void onCharacterLogin() {
    current = new CharacterSession();
    current.start = System.currentTimeMillis();
  }

  /**
   * Informs this when the player-character is no longer being played.
   * <p>
   * It is essential to fully report character logouts for accurate calculations.
   */
  public void onCharacterLogout() {
    if (current != null) {
      current.end = System.currentTimeMillis();
      sessions.add(current);
      current = null;
    }
  }

  /**
   * Informs this of the player quitting.
   * <p>
   * It is essential to fully report player quits for accurate calculations.
   */
  public void onPlayerQuit() {
    onCharacterLogout();
    this.playerLogout = System.currentTimeMillis();
  }

  private long getCharacterPlayTime(long since) {
    long total = 0;
    for (CharacterSession sess : sessions) {
      // overlaps with this session
      if (since < sess.end) {
        total += sess.getDuration(since);
      }
    }

    // time in the current, not yet finished session
    if (current != null) {
      total += current.getDuration(since);
    }
    return total;
  }

  /**
   * Stores the start and end of a session of playing a character.
   */
  private class CharacterSession {
    private Long start;
    private Long end;

    private long getDuration(long since) {
      // if this session has not yet been given a definitive end, uses the current time as the
      // current end of the session
      long useEnd;
      if (end != null) {
        useEnd = end;
      } else {
        useEnd = System.currentTimeMillis();
      }

      if (since < start) {
        // encompasses the entire session
        return useEnd - start;

      } else {
        // encompasses only part of the session
        return useEnd - since;
      }
    }
  }
}
