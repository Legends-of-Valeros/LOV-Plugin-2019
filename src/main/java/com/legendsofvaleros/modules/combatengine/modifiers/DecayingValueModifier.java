package com.legendsofvaleros.modules.combatengine.modifiers;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A modifier that changes at a constant rate over time until it reaches its ending point.
 * <p>
 * The parameters of a decaying modifier cannot be changed after it is applied. However, it can
 * still be cancelled prematurely.
 */
public class DecayingValueModifier extends ValueModifier {

  private DecayTask decayTask;

  /**
   * Class constructor that creates a permanent effect.
   * 
   * @param modify The specific value being modified.
   * @param type The type of modifier this should be.
   * @param startingValue How much to modify the value by.
   * @param removeOnDeath <code>true</code> if this should be removed when the entity dies.
   * @param endingValue The value that this should decay/appreciate towards.
   * @param decayInterval The amount of time between each update of the modified value.
   * @param decayDurationTicks The total amount of time in ticks to reach the ending value.
   */
  DecayingValueModifier(ModifiableDouble modify, ValueModifierBuilder.ModifierType type, double startingValue,
                        boolean removeOnDeath, double endingValue, long decayInterval, long decayDurationTicks) {
    super(modify, type, startingValue, removeOnDeath);
    decayTask = new DecayTask(startingValue, endingValue, decayInterval, decayDurationTicks);
  }

  /**
   * Class constructor that creates an effect that will expire after a define amount of time.
   * 
   * @param modify The specific value being modified.
   * @param type The type of modifier this should be.
   * @param startingValue How much to modify the value by.
   * @param removeOnDeath <code>true</code> if this should be removed when the entity dies.
   * @param endingValue The value that this should decay/appreciate towards.
   * @param decayInterval The amount of time between each update of the modified value.
   * @param decayDurationTicks The total amount of time in ticks to reach the ending value.
   * @param expireAfterTicks The amount of time in ticks that this should last for.
   */
  DecayingValueModifier(ModifiableDouble modify, ValueModifierBuilder.ModifierType type, double startingValue,
                        boolean removeOnDeath, double endingValue, long decayInterval, long decayDurationTicks,
                        long expireAfterTicks) {
    super(modify, type, startingValue, removeOnDeath, expireAfterTicks);

    if (decayDurationTicks > expireAfterTicks) {
      throw new IllegalArgumentException(
          "the decay's ending point cannot be after the effects expiration");
    }

    decayTask = new DecayTask(startingValue, endingValue, decayInterval, decayDurationTicks);
  }

  /**
   * The value the modifier started at.
   * 
   * @return The modifier's starting value.
   */
  public double getStartingValue() {
    return decayTask.startingValue;
  }

  /**
   * The value the modifier is heading towards.
   * 
   * @return The modifier's final value, that it will reach at the end of its decay/appreciation.
   */
  public double getEndingValue() {
    return decayTask.endingValue;
  }

  /**
   * Gets how often the value of the effect is updated, in ticks (usually 1/20 of a second).
   * <p>
   * Change from the starting point happens incrementally and linearly. If the total period is
   * <code>200</code> ticks, and this is <code>20</code>, then every <code>20</code> ticks the
   * current value will change 10% closer to its ending value.
   * 
   * @return How long passes in between each update of the value of this modifier.
   */
  public long getUpdateInterval() {
    return decayTask.interval;
  }

  /**
   * Gets the point at which this effect will (or already has) reach its ending value.
   * <p>
   * This does not necessarily correspond to an expiration of this modifier. It just signals when
   * the ending value will be reached. The effect may or may not persist after the ending point has
   * been reached.
   * 
   * @return The time at which this effect will reach its ending value, as a millisecond timestamp.
   */
  public long getEndingPoint() {
    return decayTask.endingPoint;
  }

  @Override
  protected void onRemove() {
    if (decayTask != null) {
      decayTask.cancel();
    }
  }

  /**
   * Decays/appreciates this modifier's value over time.
   */
  private class DecayTask extends BukkitRunnable {

    private final double startingValue;
    private final long endingPoint;
    private final long interval;

    private double endingValue;
    private boolean upOrDown;
    private double perInterval;

    private DecayTask(double startingValue, double endingValue, long interval, long duration) {
      if (duration < interval) {
        throw new IllegalArgumentException(
            "decay interval cannot be more than the its total duration");
      }

      this.startingValue = startingValue;
      this.endingPoint = System.currentTimeMillis() + (duration * 50);
      this.interval = interval;
      this.endingValue = endingValue;

      upOrDown = endingValue > startingValue;
      long numIntervals = duration / interval;
      perInterval = (endingValue - startingValue) / numIntervals;

      runTaskTimer(LegendsOfValeros.getInstance(), interval, interval);
    }

    @Override
    public void run() {
      double change = perInterval;
      if ((upOrDown && currentModifier + perInterval >= endingValue)
          || (!upOrDown && currentModifier + perInterval <= endingValue)) {

        change = endingValue - currentModifier;
        cancel();
      }

      switch (getType()) {
        case FLAT_EDIT:
          valueModifying.flatEdit(change, false);
          break;

        case FLAT_EDIT_IGNORES_MULTIPLIERS:
          valueModifying.flatEdit(change, true);
          break;

        case MULTIPLIER:
          valueModifying.editMultiplier(currentModifier, currentModifier + change);
          break;
      }

      currentModifier += change;
    }
  }

}
