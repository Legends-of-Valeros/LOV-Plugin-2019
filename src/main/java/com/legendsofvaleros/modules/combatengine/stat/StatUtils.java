package com.legendsofvaleros.modules.combatengine.stat;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

/**
 * Common utilities for working with, analyzing, and manipulating stats.
 */
public class StatUtils {

  /**
   * Converts between a conventional vanilla amount of health (1.0 = 1/2 a heart, 2.0 = 1 heart,
   * etc.) and a CombatEngine health value for an entity.
   * <p>
   * For example, using this method I could get how much CombatEngine health I would need to add to
   * a player to heal them by 2 hearts by using <code>convertHealth(entity, 4.0, false);</code>.
   * Alternatively, I could get how many hearts a player would lose if they took <code>300.0</code>
   * CombatEngine damage by using <code>convertHealth(entity, 300.0, true);</code>
   * 
   * @param entity The entity to convert health values for.
   * @param convert The amount of health to convert.
   * @param convertToVanilla <code>true</code> to convert a CombatEngine health value to a vanilla
   *        health value, <code>false</code> to convert a vanilla health value to a CombatEngine
   *        health value.
   * @return For the given entity, the currently equivalent amount of CombatEngine health for the
   *         given amount of vanilla health or the currently equivalent amount of vanilla health for
   *         the given amount of CombatEngine health. <code>0</code> if no stats are found for the
   *         given entity.
   */
  public static double convertHealth(LivingEntity entity, double convert, boolean convertToVanilla) {
    CombatEntity ce;
    if (entity == null || (ce = CombatEngine.getEntity(entity)) == null) {
      return 0;
    }

    double ratio;
    if (convertToVanilla) {
      ratio = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / ce.getStats().getStat(Stat.MAX_HEALTH);
    } else {
      ratio = ce.getStats().getStat(Stat.MAX_HEALTH) / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    // TODO debug
    /*
     * if (convertToVanilla) { System.out.println("Converting to vanilla health. vanillaMax=" +
     * entity.getMaxHealth() + ", ceMax =" + ce.getStats().getStat(Stat.MAX_HEALTH) + ", ratio=" +
     * ratio + ", convert=" + convert + ", result=" + (ratio * convert)); }
     */

    return ratio * convert;
  }
}
