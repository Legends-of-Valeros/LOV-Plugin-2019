package com.legendsofvaleros.modules.levelarchetypes.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.modules.combatengine.core.CombatProfile;
import com.legendsofvaleros.modules.combatengine.stat.Stat;

import java.util.HashMap;
import java.util.Map;

/**
 * Dynamically generates combat profiles based on a set of base stats, a set of
 * growth-rates-per-level, and an entity's level.
 * <p>
 * Caches generated profiles for reuse.
 */
public class CombatProfileGenerator {

  private static final Stat[] STATS = Stat.values();
  private static final int MAX_CACHE_SIZE = 100;

  private Map<Stat, LevelingValue> values;

  private Cache<Integer, CombatProfile> cachedProfiles;

  /**
   * Class constructor.
   * 
   * @param levelingValues A map of the the values for the generated combat profiles' stats.
   */
  public CombatProfileGenerator(Map<Stat, LevelingValue> levelingValues) {
    if (levelingValues == null) {
      throw new IllegalArgumentException("params cannot be null");
    }

    this.values = new HashMap<>(levelingValues);

    cachedProfiles =
        CacheBuilder.newBuilder().concurrencyLevel(2).maximumSize(MAX_CACHE_SIZE).build();
  }

  /**
   * Dynamically gets or creates a combat profile based at the requested level based on the base
   * stats and change multipliers defined.
   * 
   * @param level The level to get a combat profile for.
   * @return A combat profile for the given level.
   */
  public CombatProfile getCombatProfile(int level) {
    CombatProfile ret = cachedProfiles.getIfPresent(level);
    if (ret != null) {
      return ret;
    } else {
      ret = new CombatProfile();
    }

    cachedProfiles.put(level, ret);

    for (Stat stat : STATS) {

      LevelingValue value = values.get(stat);
      if (value != null) {
        ret.setBaseStat(stat, value.getValue(level));
      }
    }

    return ret;
  }

}
