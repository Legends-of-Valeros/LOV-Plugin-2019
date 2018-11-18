package com.legendsofvaleros.modules.levelarchetypes.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * A value that changes based on an integer level.
 * <p>
 * Has a default percentage-growth rate, but also supports per-level expeptions to this default.
 */
public class LevelingValue {

  private static final int MAX_CACHE_SIZE = 100;

  private double baseValue;
  private double defaultIncrease;
  private double defaultMultiplier;
  private Map<Integer, Double> exceptionsIncrease;
  private Map<Integer, Double> exceptionsMultiplier;

  private Cache<Integer, Double> cachedValues;

  /**
   * Class constructor that only defines a base value.
   * <p>
   * The base value will not change by level until a multiplier or exceptions are set with
   * {@link #setDefaultMultiplier(double)} or {@link #addException(int, double)}, respectively.
   * 
   * @param baseValue The base value of this leveling value, what it is at the minimum level.
   */
  public LevelingValue(double baseValue) {
    this(baseValue, 0.0, 1.0);
  }

  /**
   * Class constructor that defines a default multiplier.
   * 
   * @param baseValue The base value of this leveling value, what it is at the minimum level.
   * @param defaultMultiplierPerLevel The default amount that the previous level's value should be
   *        multiplied by to get the next level's value.
   */
  public LevelingValue(double baseValue, double defaultIncreasePerLevel, double defaultMultiplierPerLevel) {
    this.baseValue = baseValue;
    this.defaultIncrease = defaultIncreasePerLevel;
    this.defaultMultiplier = defaultMultiplierPerLevel;

    this.exceptionsIncrease = new HashMap<>();
    this.exceptionsMultiplier = new HashMap<>();

    cachedValues =
        CacheBuilder.newBuilder().concurrencyLevel(2).maximumSize(MAX_CACHE_SIZE).build();
  }

  /**
   * Dynamically gets or calculates a value based at the requested level from the base value and
   * change multipliers defined.
   * 
   * @param level The level to get a value for.
   * @return The value of this for the given level.
   */
  public double getValue(int level) {
    if (level <= LevelArchetypes.MIN_LEVEL) {
      return baseValue;
    }

    Double fromCache = cachedValues.getIfPresent(level);
    if (fromCache != null) {
      return fromCache;
    }

    double ret = baseValue;
    for (int i = LevelArchetypes.MIN_LEVEL + 1; i <= level; i++) {

        Double exception = exceptionsIncrease.get(i);
        if (exception != null) {
          ret += exception;
        } else {
          ret += defaultIncrease;
        }

      exception = exceptionsMultiplier.get(i);
      if (exception != null) {
        ret *= exception;
      } else {
        ret *= defaultMultiplier;
      }
    }

    cachedValues.put(level, ret);

    return ret;
  }

  /**
   * Adds an exception multiplier that should override the default change multiplier at a given
   * level.
   * 
   * @param level The level at which the exception should apply.
   * @param increase The exception increase for the given level.
   * @throws IllegalArgumentException If an exception at the given level already exists.
   */
  public void addExceptionIncrease(int level, double increase) throws IllegalArgumentException {
    if (exceptionsIncrease.containsKey(level)) {
      throw new IllegalArgumentException("duplicate change exception at level " + level
          + ". You cannot have to exceptions for the same stat at the same level.");
    }
    cachedValues.invalidateAll();
    exceptionsIncrease.put(level, increase);
  }

  /**
   * Sets this values default increase that will be applied to the previous level's value at each
   * incrementation of the level.
   * 
   * @param defaultIncreasePerLevel The default increase applied to the previous level's value
   *        to get a given level's value.
   */
  public void setDefaultIncrease(double defaultIncreasePerLevel) {
    cachedValues.invalidateAll();
    this.defaultIncrease = defaultIncreasePerLevel;
  }

  /**
   * Adds an exception multiplier that should override the default change multiplier at a given
   * level.
   * 
   * @param level The level at which the exception should apply.
   * @param multiplier The exception multiplier for the given level.
   * @throws IllegalArgumentException If an exception at the given level already exists.
   */
  public void addExceptionMultiplier(int level, double multiplier) throws IllegalArgumentException {
    if (exceptionsMultiplier.containsKey(level)) {
      throw new IllegalArgumentException("duplicate percentage-change exception at level " + level
          + ". You cannot have to exceptions for the same stat at the same level.");
    }
    cachedValues.invalidateAll();
    exceptionsMultiplier.put(level, multiplier);
  }

  /**
   * Sets this values default multiplier that will be applied to the previous level's value at each
   * incrementation of the level.
   * 
   * @param defaultMultiplierPerLevel The default multiplier applied to the previous level's value
   *        to get a given level's value. The compounded rate of growth per level for the base
   *        value.
   */
  public void setDefaultMultiplier(double defaultMultiplierPerLevel) {
    cachedValues.invalidateAll();
    this.defaultMultiplier = defaultMultiplierPerLevel;
  }

}
