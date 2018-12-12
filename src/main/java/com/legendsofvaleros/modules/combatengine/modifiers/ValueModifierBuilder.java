package com.legendsofvaleros.modules.combatengine.modifiers;


/**
 * Builds a modifier for a modifiable double value.
 * <p>
 * Allows for selection from a broad range of options for possible value modifications.
 */
public class ValueModifierBuilder {

  private final ConstructionListener informOfBuild;
  private final ModifiableDouble modify;

  // general settings
  private Double startingValue;
  private ModifierType type;
  private boolean removeOnDeath;

  // expiration settings
  private long expireAfterTicks;

  // decay settings
  private boolean decay;
  private double endingValue;
  private long decayInterval;
  private long decayDurationTicks;

  public ValueModifierBuilder(ModifiableDouble modify, ConstructionListener informOfBuild) {
    this.modify = modify;
    this.informOfBuild = informOfBuild;

    expireAfterTicks = Long.MAX_VALUE;
  }

  /**
   * Finishes and applies the value modifier.
   * <p>
   * All mandatory fields that do not have a default value must be set in order for this to work.
   * 
   * @return A fully constructed and applied modifier.
   * @throws IllegalStateException If any mandatory field has not been set.
   */
  public ValueModifier build() throws IllegalStateException {
    ValueModifier ret;

    if (type == null || startingValue == null) {
      throw new IllegalStateException(
          "Cannot build value modifier. At least one mandatory field has not been set to a valid value.");
    }

    if (!decay) {
      if (expireAfterTicks < Long.MAX_VALUE) {
        ret =
            new EditableValueModifier(modify, type, startingValue, removeOnDeath, expireAfterTicks);

      } else {
        ret = new EditableValueModifier(modify, type, startingValue, removeOnDeath);
      }

    } else {
      if (expireAfterTicks < Long.MAX_VALUE) {
        ret =
            new DecayingValueModifier(modify, type, startingValue, removeOnDeath, endingValue,
                decayInterval, decayDurationTicks, expireAfterTicks);

      } else {
        ret =
            new DecayingValueModifier(modify, type, startingValue, removeOnDeath, endingValue,
                decayInterval, decayDurationTicks);
      }
    }

    if (informOfBuild != null) {
      informOfBuild.onConstruction(ret);
    }

    return ret;
  }

  /**
   * Sets the type of modifier this will be.
   * <p>
   * Mandatory. This must be set before the modifier can be built. There is no default value.
   * 
   * @param type The type of modifier.
   * @return This builder.
   */
  public ValueModifierBuilder setModifierType(ModifierType type) {
    this.type = type;
    return this;
  }

  /**
   * Sets the value of the modifier.
   * <p>
   * The effect of the value will depend on this modifier's type, see
   * {@link #setModifierType(ModifierType)}.
   * <p>
   * Mandatory. This must be set before the modifier can be built. There is no default value.
   * 
   * @param value The value of the modifier.
   * @return This builder.
   */
  public ValueModifierBuilder setValue(double value) {
    this.startingValue = value;
    return this;
  }

  /**
   * Sets the duration of this effect, in ticks, from when it is built.
   * <p>
   * After the duration, the effect will expire and be removed.
   * <p>
   * Defaults to <code>Long.MAX_VALUE</code> for no expiration (the effect will last until it is
   * manually removed).
   * 
   * @param durationTicks The amount of time this effect should last before expiring on its own, in
   *        ticks.
   * @return This builder.
   */
  public ValueModifierBuilder setDuration(long durationTicks) {
    this.expireAfterTicks = durationTicks;
    return this;
  }

  /**
   * Sets whether this effect should be removed when the entity dies.
   * <p>
   * Defaults to <code>false</code> (this effect will not be removed on death).
   * 
   * @param removeOnDeath <code>true</code> to remove this modifier on death, else
   *        <code>false</code>.
   * @return This builder.
   */
  public ValueModifierBuilder setRemovedOnDeath(boolean removeOnDeath) {
    this.removeOnDeath = removeOnDeath;
    return this;
  }

  /**
   * Sets this effect to decay/appreciate its value over time.
   * <p>
   * The modifier will start at the value set with {@link #setValue(double)} and over time
   * grow/shrink linearly until it reaches its ending value.
   * <p>
   * Defaults to no decay (this effect will keep the same value until removed).
   * 
   * @param endingValue The value that this modifier will move towards over time.
   * @param interval How long should pass in between updates of this modifier's value, in ticks
   *        (usually 1/20 of a second). If the time to reach the end value is <code>200</code>
   *        ticks, and this is <code>20</code>, then every <code>20</code> ticks the current value
   *        will change 10% closer to its ending value (because the interval is 1/10 of the total
   *        time). A shorter interval will cause slightly more load on the server.
   * @param ticksToReachEndValue The total amount of time it should take before this modifier
   *        reaches its ending value, in ticks (usually 1/20 of a second). Must be longer than the
   *        interval.
   * @return This builder.
   * @throws IllegalArgumentException On an interval that is longer than the decay's total duration
   *         or a decay's total duration that is longer than the effect's total duration (set with
   *         {@link #setDuration(long)}.
   */
  public ValueModifierBuilder setDecay(double endingValue, long interval, long ticksToReachEndValue)
      throws IllegalArgumentException {
    if (ticksToReachEndValue < interval) {
      throw new IllegalArgumentException(
          "decay interval cannot be more than the its total duration");
    } else if (ticksToReachEndValue > expireAfterTicks) {
      throw new IllegalArgumentException(
          "the decay's ending point cannot be after the effects expiration");
    }

    this.decay = true;
    this.endingValue = endingValue;
    this.decayInterval = interval;
    this.decayDurationTicks = ticksToReachEndValue;

    return this;
  }

  /**
   * Types of stat modifiers.
   */
  public enum ModifierType {

    /**
     * Adds or subtracts to/from the modified value by some set amount. Affected by multipliers.
     */
    FLAT_EDIT("+"),

    /**
     * Multiplies the modified value.
     */
    MULTIPLIER("*"),

    /**
     * Adds or subtracts to/from the modified value by some set amount. Not affected by multipliers.
     */
    FLAT_EDIT_IGNORES_MULTIPLIERS("(+)");

    final String sign;

    ModifierType(String sign) {
      this.sign = sign;
    }

    public String getSign() {
      return sign;
    }
  }

}
