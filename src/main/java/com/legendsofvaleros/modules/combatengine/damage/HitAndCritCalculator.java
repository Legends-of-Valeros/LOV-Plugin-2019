package com.legendsofvaleros.modules.combatengine.damage;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.config.CriticalHitConfig;
import com.legendsofvaleros.modules.combatengine.config.HitChanceConfig;
import com.legendsofvaleros.modules.combatengine.stat.Stat;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Calculates the hit and crit chances for attacks.
 */
public class HitAndCritCalculator {

  // what to divide the chance stats by to get their percentage value as a decimal
  private static final double DIVISOR = 100.0;

  private final HitChanceConfig hitConfig;
  private final CriticalHitConfig critConfig;

  public HitAndCritCalculator(HitChanceConfig hitConfig, CriticalHitConfig critConfig) {
    this.hitConfig = hitConfig;
    this.critConfig = critConfig;
  }

  /**
   * Gets the hit chance for an attack.
   * 
   * @param target The entity being attacked.
   * @param attacker The attacker. Can be <code>null</code> if the attack's source is ambiguous or
   *        it is not directly from another entity.
   * @return The chance of the hit landing, as a decimal (<code>0.5</code> for a 50% chance). If the
   *         hit does not land, it should miss.
   */
  public double getHitChance(CombatEntity target, CombatEntity attacker) {
    double chanceAgainst = target.getStats().getStat(Stat.DODGE_CHANCE) / DIVISOR;

    double chanceFor =
        attacker != null ? attacker.getStats().getStat(Stat.HIT_CHANCE) / DIVISOR : hitConfig
            .getUnattributedHitChance();

    return chanceFor - chanceAgainst;
  }

  /**
   * Gets the crit chance for an attack.
   * 
   * @param target The entity being attacked.
   * @param attacker The attacker. Can be <code>null</code> if the attack's source is ambiguous or
   *        it is not directly from another entity.
   * @return The chance of a crit landing for a hit, as a decimal (<code>0.5</code> for a 50%
   *         chance).
   */
  public double getCritChance(CombatEntity target, CombatEntity attacker) {
    if (attacker != null) {
      return attacker.getStats().getStat(Stat.CRIT_CHANCE) / DIVISOR;
    } else {
      return critConfig.getUnattributedCritChance();
    }
  }

  /**
   * Gets whether an attack should hit, based on the hit chance calculated from the attacker and
   * defender's stats.
   * <p>
   * Convenience method that takes the chance for an attack to hit and picks a pseudo-random result.
   * Will return an unpredictable result that, on average and over time, will conform to the
   * percentage chance of a critical hit for the given entity(s).
   * 
   * @param target The entity being attacked.
   * @param attacker The attacker. Can be <code>null</code> if the attack's source is ambiguous or
   *        it is not directly from another entity.
   * @return <code>true</code> if the attack should land, else <code>false</code>.
   */
  public boolean doesAttackHit(CombatEntity target, CombatEntity attacker) {
    return runChance(getHitChance(target, attacker));
  }

  /**
   * Gets whether an attack should crit, based on the crit chance calculated from the attacker and
   * defender's stats.
   * <p>
   * Convenience method that takes the chance for an attack to crit and picks a pseudo-random
   * result. Will return an unpredictable result that, on average and over time, will conform to the
   * percentage chance of a critical hit for the given entity(s).
   * 
   * @param target The entity being attacked.
   * @param attacker The attacker. Can be <code>null</code> if the attack's source is ambiguous or
   *        it is not directly from another entity.
   * @return <code>true</code> if the attack should be a critical hit, else <code>false</code>.
   */
  public boolean doesAttackCrit(CombatEntity target, CombatEntity attacker) {
    return runChance(getCritChance(target, attacker));
  }

  private boolean runChance(double chance) {
    return ThreadLocalRandom.current().nextDouble() < chance;
  }

}
