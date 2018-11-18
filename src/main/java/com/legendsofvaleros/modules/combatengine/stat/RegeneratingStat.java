package com.legendsofvaleros.modules.combatengine.stat;

import java.util.HashMap;
import java.util.Map;

/**
 * A stat that regenerates over time.
 * <p>
 * These stats must always have a value in between <code>0</code> and another stat whose value is
 * the max for each entity.
 */
public enum RegeneratingStat {

  HEALTH("Health", "HP", Stat.MAX_HEALTH, Stat.HEALTH_REGEN),
  MANA("Mana", "MP", Stat.MAX_MANA, Stat.MANA_REGEN),
  ENERGY("Energy", "EP", Stat.MAX_ENERGY, Stat.ENERGY_REGEN);

  private static Map<Stat, RegeneratingStat> maxMap;
  private static Map<Stat, RegeneratingStat> regenMap;

  private String uiVersion, uiTag;
  private Stat max;
  private Stat regen;

  RegeneratingStat(String uiVersion, String uiTag, Stat max, Stat regen) {
    this.uiVersion = uiVersion;
    this.uiTag = uiTag;
    this.max = max;
    this.regen = regen;

    populateMaps(max, regen, this);
  }

  private static void populateMaps(Stat max, Stat regen, RegeneratingStat thisStat) {
    if (maxMap == null) {
      maxMap = new HashMap<>();
    }
    if (regenMap == null) {
      regenMap = new HashMap<>();
    }

    maxMap.put(max, thisStat);
    regenMap.put(regen, thisStat);
  }

  /**
   * Gets a regenerating stat using its max version as an identifier.
   * 
   * @param stat The stat to get the regenerating version of.
   * @return The regenerating version of the stat, if one is found. Else <code>null</code>.
   */
  public static RegeneratingStat getFromMax(Stat stat) {
    return maxMap.get(stat);
  }

  /**
   * Gets a regenerating stat using its regen-rate stat as an identifier.
   * 
   * @param stat The stat to get the regenerating version of.
   * @return The regenerating version of the stat, if one is found. Else <code>null</code>.
   */
  public static RegeneratingStat getFromRegen(Stat stat) {
    return regenMap.get(stat);
  }

  /**
   * Gets a user-friendly name for this stat that can be used in user interfaces.
   * 
   * @return The user-friendly name of this stat.
   */
  public String getUserFriendlyName() {
    return uiVersion;
  }

  public String getUserFriendlyTag() {
    return uiTag;
  }

  /**
   * Gets the stat whose value is the max this regenerating stat can be for each entity.
   * 
   * @return The stat that is the upper ceiling of this stat.
   */
  public Stat getMaxStat() {
    return max;
  }

  /**
   * Gets the stat whose value dictates how quickly this stat regenerates over time.
   * 
   * @return The stat that defines this stat's regen rate.
   */
  public Stat getRegenStat() {
    return regen;
  }

  /**
   * Takes the value of this stat and formats it in the best way for use in user displays, specific
   * to how this stat is used and thought of.
   * <p>
   * For example, some stats should be displayed as integers with any trailing decimals rounded to
   * the nearest whole number. Others might be best displayed as a percentage rounded to the nearest
   * hundredth.
   * 
   * @param value The value to make into a user-friendly string.
   * @return A user-friendly version of the given value for a stat.
   */
  public String formatForUserInterface(double value) {
    if (value > 1 || value <= 0) {
      return String.valueOf(Math.round(value));
    } else {
      return "1";
    }
  }

}
