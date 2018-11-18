package com.legendsofvaleros.modules.characters.api;

/**
 * A set of cooldowns for a player-character.
 */
public interface Cooldowns {

  /**
   * Gets the PlayerCharacter that these cooldowns are for.
   * 
   * @return The player character that these cooldowns affect.
   */
  PlayerCharacter getPlayerCharacter();

  /**
   * Gets whether there is a cooldown for a given key.
   * 
   * @param key The key to check for a corresponding cooldown.
   * @return <code>true</code> if there is already a cooldown with the given key. Else
   *         <code>false</code>.
   * @throws IllegalStateException If the player these cooldowns is for is not online.
   */
  boolean hasCooldown(String key) throws IllegalStateException;

  /**
   * Gets the cooldown for a given key, if there is one.
   * 
   * @param key The key of the cooldown to get.
   * @return The cooldown for the given key, if one is found. Else <code>null</code>.
   * @throws IllegalStateException If the player these cooldowns is for is not online.
   */
  Cooldown getCooldown(String key) throws IllegalStateException;

  /**
   * Adds a cooldown for the given key, if there is not already a cooldown for that key.
   * 
   * @param key The key to add a cooldown for.
   * @param type The type of cooldown to add.
   * @param durationMillis How long the cooldown should last, in milliseconds.
   * @return The newly created cooldown, if there was not already a cooldown and the offered
   *         cooldown was successfully added. <code>null</code> if there was already a cooldown for
   *         the given key and the new cooldown was not added.
   * @throws IllegalStateException If the player these cooldowns is for is not online.
   */
  Cooldown offerCooldown(String key, CooldownType type, long durationMillis)
      throws IllegalStateException;

  /**
   * Adds a cooldown for the given key, overwriting any previous cooldown for that key.
   * 
   * @param key The key to add a cooldown for.
   * @param type The type of cooldown to add.
   * @param durationMillis How long the cooldown should last, in milliseconds.
   * @return The newly created cooldown.
   * @throws IllegalStateException If the player these cooldowns is for is not online.
   */
  Cooldown overwriteCooldown(String key, CooldownType type, long durationMillis)
      throws IllegalStateException;

  /**
   * An instance of a cooldown that expires over time.
   */
  interface Cooldown {

    /**
     * Gets the set of cooldowns that this cooldown is a member of.
     * 
     * @return This cooldown's parent set of cooldowns.
     */
    Cooldowns getCooldowns();

    /**
     * Gets the key of this cooldown.
     * 
     * @return This cooldown's key.
     */
    String getKey();

    /**
     * Gets the type of cooldown that this is (how players are able to run down the cooldown's
     * timer).
     * 
     * @return This cooldown's type.
     */
    CooldownType getCooldownType();

    /**
     * Gets how many milliseconds are left until this cooldown expires.
     * <p>
     * Note that the actual amount of time it may take for this to cooldown will depend on what type
     * of cooldown it is. If there are <code>1000</code> milliseconds left, a player needs to be
     * playing for those <code>1000</code> milliseconds, and they are not, then the cooldown will
     * not tick down until they are playing.
     * 
     * @return The remaining time of this cooldown, in milliseconds. <code>0</code> if this has
     *         already expired.
     */
    long getRemainingDurationMillis();

    /**
     * Forces this cooldown to expire early, if it has not already.
     */
    void expire();

    /**
     * Registers a listener to be informed when this cooldown expires. Does nothing if this cooldown
     * has already expired.
     * <p>
     * If this cooldown is overwritten by another cooldown, the listener will listen for the new
     * cooldown's expiration without being called first.
     * <p>
     * If this cooldown is cancelled prematurely, the listener will be informed.
     * <p>
     * If the player logs out before the cooldown expires, the listener will <i>not</i> be informed
     * if/when it does expire, unless the player logs back on and the listener is registered again.
     * 
     * @param listener The listener to inform when this cooldown or cooldowns that overwrite it
     *        expire.
     */
    void registerListener(CooldownExpirationListener listener);

  }

  /**
   * Informed when a cooldown expires.
   */
  interface CooldownExpirationListener {

    /**
     * Called when a cooldown expires.
     * 
     * @param expired The cooldown that expired. This object may have a different identity to the
     *        one that was originally listened to; it should be identified by its string key and
     *        player-character rather than the <code>Cooldown</code> object's identity.
     */
    void onExpiration(Cooldown expired);

  }

  /**
   * The different types of cooldowns.
   */
  enum CooldownType {

    /**
     * Expires in calendar time, whether the player is playing or not.
     */
    CALENDAR_TIME(),

    /**
     * Counts down only while the player is online the server/network, but regardless of which
     * character they are playing.
     */
    PLAYER_PLAY_TIME(),

    /**
     * Counts down only while the player is logged in as the specific character that the cooldown is
     * for.
     */
    CHARACTER_PLAY_TIME()

  }
}
