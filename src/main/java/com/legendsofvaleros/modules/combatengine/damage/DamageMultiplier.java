package com.legendsofvaleros.modules.combatengine.damage;

import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.config.CriticalHitConfig;
import com.legendsofvaleros.modules.combatengine.config.OffenseDefenseConfig;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;

import java.util.HashMap;
import java.util.Map;

/**
 * Combines the offensive and defensive stats of an attacker and defender to determine how much an
 * attacks base damage should be multiplied by, either increasing or decreasing its base value.
 */
public class DamageMultiplier {

  private final OffenseDefenseConfig odConfig;
  private final CriticalHitConfig chConfig;

  // avoids repeatedly making defensive copies of the values within the SpellType class
  private final Map<SpellType, Stat[]> applicableResistances;

  public DamageMultiplier(OffenseDefenseConfig odConfig, CriticalHitConfig chConfig) {
    this.odConfig = odConfig;
    this.chConfig = chConfig;

    applicableResistances = new HashMap<>();
    for (SpellType type : SpellType.values()) {
      applicableResistances.put(type, type.getApplicableResistances());
    }
  }

  /**
   * Gets the multiplier for an instance of spell damage, based on its type and the attacker and
   * defender's stats.
   * 
   * @param target The affected entity.
   * @param attacker The attacker. Can be <code>null</code> if the damage was ambiguous or not
   *        directly caused by another entity.
   * @param criticalHit <code>true</code> if this is a critical hit and the critical-hit bonus
   *        multiplier should be added, else <code>false</code>.
   * @param spellType The type of spell damage.
   * @return A single, final multiplier for the damage based on all of the attacker's and defender's
   *         factors combined.
   */
  public double getSpellDamageMultiplier(CombatEntity target, CombatEntity attacker,
      boolean criticalHit, SpellType spellType) {

    double attackBonus = 1;
    if (attacker != null) {
      attackBonus +=
          attacker.getStats().getStat(Stat.MAGIC_ATTACK) * odConfig.getMagicDamageIncrease();
    }

    double armorPenalty =
        1 - (target.getStats().getStat(Stat.ARMOR) * odConfig.getArmorSpellDamageReduction());

    Stat[] resistances = applicableResistances.get(spellType);
    double resistPenalty = 1.0;
    for (Stat resist : resistances) {
      if (resist != null) {
        resistPenalty *=
                1 - (target.getStats().getStat(resist) * odConfig.getResistanceSpellDamageReduction());
      }
    }

    double critBonus = criticalHit ? chConfig.getCritMultiplier() : 1.0;

    return attackBonus * armorPenalty * resistPenalty * critBonus;
  }

  /**
   * Gets the multiplier for an instance of physical damage, based on its type and the attacker and
   * defender's stats.
   * 
   * @param target The affected entity.
   * @param attacker The attacker. Can be <code>null</code> if the damage was ambiguous or not
   *        directly caused by another entity.
   * @param criticalHit <code>true</code> if this is a critical hit and the critical-hit bonus
   *        multiplier should be added, else <code>false</code>.
   * @param physicalType The type of physical damage.
   * @return A single, final multiplier for the damage based on all of the attacker's and defender's
   *         factors combined.
   */
  public double getPhysicalDamageMultiplier(CombatEntity target, CombatEntity attacker,
      boolean criticalHit, PhysicalType physicalType) {

    double attackBonus = 1;
    if (attacker != null) {
      attackBonus +=
          attacker.getStats().getStat(Stat.PHYSICAL_ATTACK) * odConfig.getPhysicalDamageIncrease();
    }

    double armorPenalty =
        1 - (target.getStats().getStat(Stat.ARMOR) * odConfig.getArmorPhysicalDamageReduction());

    double critBonus = criticalHit ? chConfig.getCritMultiplier() : 1.0;

    return attackBonus * armorPenalty * critBonus;
  }

}
