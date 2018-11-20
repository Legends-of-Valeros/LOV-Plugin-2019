package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * A profile which can be used as a starting point for an entity's stats.
 */
public class CombatProfile {

  private Map<Stat, Double> statValues;
  private Map<RegeneratingStat, Double> regeneratingStatValues;

  /**
   * Class constructor that makes an empty combat profile.
   */
  public CombatProfile() {
    this.statValues = new HashMap<>();
    this.regeneratingStatValues = new HashMap<>();
  }

  /**
   * Class constructor that copies from another combat profile.
   * 
   * @param copyFrom The combat profile to copy stats from.
   */
  public CombatProfile(CombatProfile copyFrom) {
    this.statValues = new HashMap<>(copyFrom.statValues);
    regeneratingStatValues = new HashMap<>(copyFrom.regeneratingStatValues);
  }

  /**
   * Creates a combat entity, building from this profile's base stats.
   * 
   * @param entity The entity to create combat data for from this profile.
   * @return A new combat data object built from this base profile.
   */
  CombinedCombatEntity createCombatEntity(LivingEntity entity) {
    CombinedCombatEntity ce = new CombinedCombatEntity(entity);

    for (Map.Entry<Stat, Double> ent : statValues.entrySet()) {
      ce.newStatModifierBuilder(ent.getKey()).setValue(ent.getValue())
          .setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT).build();

      // if no other value is defined, assumes that regenerating stats start at their full value
      // for example: unless defined otherwise, a mob will spawn with full health
      RegeneratingStat regeneratingVersion = RegeneratingStat.getFromMax(ent.getKey());
      if (regeneratingVersion != null && !regeneratingStatValues.containsKey(regeneratingVersion)) {
        ce.editRegeneratingStat(regeneratingVersion, ent.getValue());
      }
    }

    for (Map.Entry<RegeneratingStat, Double> ent : regeneratingStatValues.entrySet()) {
      ce.editRegeneratingStat(ent.getKey(), ent.getValue());
    }

    return ce;
  }

  /**
   * Gets the base value of a regenerating stat.
   * <p>
   * Once an instance is built from this profile and regardless of the base value, a regenerating
   * stat can never be more than its max version. For example, <code>HEALTH</code> will never be
   * more than <code>MAX_HEALTH</code>.
   * 
   * @param stat The regenerating stat to get the base value of.
   * @return The regenerating stat's base value. <code>0</code> if it has not been set.
   */
  public double getBaseRegeneratingStat(RegeneratingStat stat) {
    Double value = regeneratingStatValues.get(stat);
    if (value == null) {
      return 0;
    }
    return value;
  }

  /**
   * Gets the base value of a stat.
   * 
   * @param stat The stat to get the base value of.
   * @return The stat's base value. <code>0</code> if it has not been set.
   */
  public double getBaseStat(Stat stat) {
    Double value = statValues.get(stat);
    if (value == null) {
      return 0;
    }
    return value;
  }

  /**
   * Sets the base level of a stat.
   * <p>
   * The base level is what any object built from this profile will start out with, which will then
   * be affected by its max version and be editable by clients for circumstances specific to the
   * entity the built object is for.
   * <p>
   * Once an instance is built from this profile and regardless of the base value, a regenerating
   * stat can never be more than its max version. For example, <code>HEALTH</code> will never be
   * more than <code>MAX_HEALTH</code>.
   * 
   * @param stat The regenerating stat to set the base value for.
   * @param value The base value for the regenerating stat.
   */
  public void setBaseRegeneratingStat(RegeneratingStat stat, double value) {
    if (stat != null) {
      regeneratingStatValues.put(stat, value);
    }
  }

  /**
   * Sets the base level of a stat.
   * <p>
   * The base level is what any object built from this profile will start out with, which will then
   * be editable by clients for circumstances specific to the entity the built object is for.
   * 
   * @param stat The stat to set a base value for.
   * @param value The base value for the stat.
   */
  public void setBaseStat(Stat stat, double value) {
    if (stat != null) {
      statValues.put(stat, stat.sanitizeValue(value));
    }
  }

}
