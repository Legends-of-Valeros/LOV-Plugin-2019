package com.legendsofvaleros.modules.combatengine.modifiers;

/**
 * A modifiable double value.
 * <p>
 * Describes a value that can be modified in a few different ways:
 * <ul>
 * <li>Multipliers.
 * <li>Flat edits (positive or negative) that are affected by mulitpliers.
 * <li>Flat edits that ignore multipliers.
 * </ul>
 */
public abstract class ModifiableDouble {

  private double baseValue;
  private double multiplier;
  private double ignoringMultipliers;

  // cannot divide by zero to reverse a 0.0 multiplier
  private int numZeroMultipliers;

  /**
   * Class constructor.
   */
  protected ModifiableDouble() {
    this.multiplier = 1.0;
  }

  /**
   * Gets the final value of this stat.
   * 
   * @return This stat's final value.
   */
  public final double getFinalValue() {
    if (numZeroMultipliers > 0) {
      return 0 + ignoringMultipliers;
    }
    return sanitizeValue((baseValue * multiplier) + ignoringMultipliers);
  }

  /**
   * Makes an edit to this stat's value.
   * <p>
   * In order to reverse a previous edit, make the same edit but with the opposite value (positive
   * or negative).
   * 
   * @param edit The amount to add/substract to/from the value.
   * @param ignoreMultipliers <code>true</code> for the value of the edit to ignore multipliers,
   *        else <code>false</code>. If reversing a previous edit, use the same value as the edit
   *        being reversed.
   */
  public final void flatEdit(double edit, boolean ignoreMultipliers) {
    double previousValue = getFinalValue();

    if (!ignoreMultipliers) {
      baseValue += edit;
    } else {
      ignoringMultipliers += edit;
    }

    update(previousValue);
  }

  /**
   * Adds a multiplier to this stat.
   * <p>
   * Does not affect edits that ignore multipliers.
   * 
   * @param multiplier The multiplier to add.
   */
  public final void addMultiplier(double multiplier) {
    double previousValue = getFinalValue();

    if (multiplier == 0.0) {
      numZeroMultipliers++;

    } else {
      this.multiplier *= multiplier;
    }

    update(previousValue);
  }

  /**
   * Removes a multiplier from this stat.
   * <p>
   * For example, if <code>0.5</code> is given, the resulting final value will end up being twice
   * what it previously was before <code>0.5</code> was removed.
   * <p>
   * A convenience method to reduce the amount of math you need to keep straight.
   * <p>
   * Does not affect edits that ignore multipliers.
   * 
   * @param multiplier The multiplier to remove.
   */
  public final void removeMultiplier(double multiplier) {
    double previousValue = getFinalValue();

    if (multiplier == 0) {
      numZeroMultipliers--;
      if (numZeroMultipliers < 0) {
        numZeroMultipliers = 0;
      }

    } else {
      this.multiplier /= multiplier;
    }

    update(previousValue);
  }

  /**
   * Edits an existing multiplier, atomically removing a previous multiplier and replacing it with a
   * new one.
   * <p>
   * Allows multipliers to be changed and updated without multiple operations/updates.
   * <p>
   * Avoids updating any user interfaces with partially-updated values.
   * 
   * @param previousMultiplier The multiplier to remove.
   * @param newMultiplier The multiplier to replace it with.
   */
  public final void editMultiplier(double previousMultiplier, double newMultiplier) {
    if (previousMultiplier == newMultiplier) {
      return;
    }

    double previousValue = getFinalValue();

    if (previousMultiplier == 0.0) {
      numZeroMultipliers--;
      if (numZeroMultipliers < 0) {
        numZeroMultipliers = 0;
      }

    } else {
      this.multiplier /= previousMultiplier;
    }

    if (newMultiplier == 0.0) {
      numZeroMultipliers++;

    } else {
      this.multiplier *= newMultiplier;
    }

    update(previousValue);
  }

  /**
   * Gets a sanitized version of this modified double.
   * 
   * @param sanitize The un-sanitized version to sanitize.
   * @return A sanitized version of the value.
   */
  protected abstract double sanitizeValue(double sanitize);

  /**
   * Called when this value changes.
   * 
   * @param newValue The new value that this has been changed to.
   * @param previousValue The previous value.
   */
  protected abstract void onChange(double newValue, double previousValue);

  // updates subclass if an actual change happened
  private void update(double previousValue) {
    double newValue = getFinalValue();
    if (previousValue != newValue) {
      onChange(newValue, previousValue);
    }
  }

}
