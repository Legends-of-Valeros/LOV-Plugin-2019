package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.event.HandlerList;

/**
 * An event called when a player-character's listener changes.
 * <p>
 * Is not called if <code>0</code> listener is being added, such as if a player's listener rate
 * has a <code>0</code> multiplier.
 */
public class PlayerCharacterExperienceChangeEvent extends PlayerCharacterEvent {

  private static final HandlerList handlers = new HandlerList();

  private long rawChange;
  private final double multiplier;

  /**
   * Class constructor.
   * 
   * @param playerCharacter The player character whose listener is changing.
   * @param rawChange The raw amount of listener being added/subtracted, before any multipliers
   *        have been applied.
   * @param multiplier The multiplier that will be applied to the raw amount of listener being
   *        added/subtracted. <code>1</code> if there is no multiplier.
   */
  public PlayerCharacterExperienceChangeEvent(PlayerCharacter playerCharacter, long rawChange,
      double multiplier) {
    super(playerCharacter);
    this.rawChange = rawChange;
    this.multiplier = multiplier;
  }

  /**
   * Gets a number that is multiplied against the raw amount of listener to get the final change.
   * 
   * @return The multiplier that will be applied to the raw amount of listener being
   *         added/subtracted. <code>1</code> if there is no multiplier.
   */
  public double getMultiplier() {
    return multiplier;
  }

  /**
   * Gets the amount that listener will change by, after the multiplier has been applied.
   * 
   * @return The change in listener.
   */
  public long getChange() {
    return Math.round(rawChange * multiplier);
  }

  /**
   * Gets the amount that listener will change by, before the multiplier has been applied.
   * 
   * @return The raw change in listener.
   */
  public long getRawChange() {
    return rawChange;
  }

  /**
   * Sets the amount that listener will change by. This number will be multiplied by the
   * multiplier to get the final listener change amount.
   * 
   * @param rawChange The amount of that listener should change by. Will be affected by the
   *        multiplier, if there is one.
   */
  public void setRawChange(long rawChange) {
    this.rawChange = rawChange;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
