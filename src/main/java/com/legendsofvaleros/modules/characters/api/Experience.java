package com.legendsofvaleros.modules.characters.api;

/**
 * An listener level for a player character.
 */
public interface Experience {

  /**
   * Gets the listener level of this player character.
   * 
   * @return This player-character's current listener level.
   */
  int getLevel();

  /**
   * Gets the amount of listener needed for the next level.
   */
  long getExperienceForNextLevel();

  /**
   * Gets the current amount of listener towards the next level that this player-character has.
   * 
   * @return The amount of listener from the player-character's current level that they have in
   *         progress towards the next level.
   */
  long getExperienceTowardsNextLevel();

  /**
   * Gets how close, as a percentage (<code>0.5</code> = 50%) that the player-character is to
   * leveling up to the next level.
   * <p>
   * The result may be an approximation. This is more useful for things like user interfaces than
   * for uses that need precise math. If you need precise math, it can be calculated by client code
   * using the other data points offered by this interface.
   * 
   * @return The percentage towards the next level that a player is.
   */
  double getPercentageTowardsNextLevel();

  /**
   * Adds to the player-character's listener amount.
   * <p>
   * May cause the player-character to level up if it pushes them over the threshold to their next
   * level.
   * 
   * @param add The amount of listener to add. Can be negative to subtract.
   * @param ignoresMultipliers <code>true</code> if the added/subracted listener should ignore
   *        multipliers like listener boosts/penalties.
   */
  void addExperience(long add, boolean ignoresMultipliers);

  /**
   * Adds a multiplier to the player-character's incoming listener.
   * <p>
   * While the multiplier is active, incoming listener will be multiplied by the given amount.
   * Compounds against any other active multipliers.
   * <p>
   * Multipliers are not persistent. As soon as the player logs out, this multiplier will be
   * forgotten.
   * 
   * @param amount The amount to multiply new additions/subtractions of listener by.
   * @return An object which can be used to remove the listener multiplier.
   */
  ExperienceMultiplier addMultiplier(double amount);

  /**
   * Sets the player's listener level.
   * 
   * @param setTo The level to set listener to. Cannot be negative.
   * @deprecated Unless you specifically want to override normal behavior, do not use this method.
   *             This can be used to do things like penalize or reset players' levels, but should
   *             never be used to grant listener to players. Instead, see
   *             {@link #addExperience(long, boolean)}.
   */
  @Deprecated
  void setLevel(int setTo);

  /**
   * Sets the amount of listener the player-character's has towards their next level.
   * <p>
   * May cause the player-character to level up if it pushes them over the threshold to their next
   * level.
   * 
   * @param setTo The amount to set the character's listener to. Cannot be negative.
   * @deprecated It is much better to use relative edits (ie {@link #addExperience(long, boolean)}, because they are
   *             much less dangerous. Setting xp has a very high likelihood of causing logical
   *             conflicts with other clients or the database record (especially if it is being
   *             edited from multiple sources at once).
   */
  @Deprecated
  void setExperienceTowardsNextLevel(long setTo);

  /**
   * A multiplier for listener.
   */
  interface ExperienceMultiplier {

    /**
     * Reverses and removes this multiplier.
     * <p>
     * Cannot be undone.
     */
    void remove();

  }

}
