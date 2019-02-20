package com.legendsofvaleros.modules.combatengine.damage;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.config.CriticalHitConfig;
import com.legendsofvaleros.modules.combatengine.config.OffenseDefenseConfig;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;

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
   * @param attacker The attacker. Can be <code>null</code> if the damage was ambiguous or not
   *        directly caused by another entity.
   * @return A single, final multiplier for the damage based on all of the attacker's and defender's
   *         factors combined.
   */
  public double getSpellDamageBonus(CombatEntity attacker, SpellType type) {
    if (attacker == null)
      return 1;
    return attacker.getStats().getStat(Stat.MAGIC_ATTACK) * odConfig.getMagicDamageIncrease();
  }

  public double getSpellDamageArmorPenalty(CombatEntity target) {
    return 1 - (target.getStats().getStat(Stat.ARMOR) * odConfig.getArmorPhysicalDamageReduction());
  }
  public double getSpellResistancePenalty(CombatEntity target, SpellType spellType) {
    Stat[] resistances = applicableResistances.get(spellType);
    double resistPenalty = 1.0;
    for (Stat resist : resistances) {
      if (resist != null) {
        resistPenalty *=
                1 - (target.getStats().getStat(resist) * odConfig.getResistanceSpellDamageReduction());
      }
    }
    return resistPenalty;
  }

  /**
   * Gets the multiplier for an instance of physical damage, based on its type and the attacker and
   * defender's stats.
   *
   * @param attacker The attacker. Can be <code>null</code> if the damage was ambiguous or not
   *        directly caused by another entity.
   * @param physicalType The type of physical damage.
   * @return A single, final multiplier for the damage based on all of the attacker's and defender's
   *         factors combined.
   */
  public double getPhysicalDamageBonus(CombatEntity attacker, PhysicalType physicalType) {
    if (attacker == null)
      return 1;
    return attacker.getStats().getStat(Stat.PHYSICAL_ATTACK) * odConfig.getPhysicalDamageIncrease();
  }

  public double getPhysicalDamageArmorPenalty(CombatEntity target) {
    return 1 - (target.getStats().getStat(Stat.ARMOR) * odConfig.getArmorPhysicalDamageReduction());
  }

  public double getCritDamageMultiplier() {
    return chConfig.getCritMultiplier();
  }
}
