package com.legendsofvaleros.modules.characters.util;

import com.legendsofvaleros.modules.combatengine.core.CombatProfile;
import com.legendsofvaleros.modules.combatengine.stat.Stat;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods relevant to progression and leveling up.
 */
public class ProgressionUtils {

  private static final Stat[] STATS = Stat.values();

  // prevents accidental construction
  private ProgressionUtils() {}

  /**
   * Gets the stat difference between two combat profiles.
   * 
   * @param subtractThis The combat profile to subtract from the other. If finding the difference in
   *        base stats from one level to another, this would be the lower level.
   * @param fromThis The combat profile to subtract from in order to find the difference. If finding
   *        the difference in base stats from one level to another, this would be the higher level.
   * @return A map of the differences between these two combat profiles.
   */
  public static Map<Stat, Double> getProfileDifference(CombatProfile subtractThis,
      CombatProfile fromThis) {
    Map<Stat, Double> ret = new HashMap<>();

    for (Stat stat : STATS) {

      ret.put(stat, fromThis.getBaseStat(stat) - subtractThis.getBaseStat(stat));
    }

    return ret;
  }
}
