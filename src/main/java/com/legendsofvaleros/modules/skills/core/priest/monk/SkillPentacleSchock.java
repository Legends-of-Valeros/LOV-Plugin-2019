package com.legendsofvaleros.modules.skills.core.priest.monk;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

/**
 * Created by Crystall on 10/30/2019
 */
public class SkillPentacleSchock extends Skill {

  public SkillPentacleSchock(String id, Type type, EntityClass pclass, int[] levelCosts, int[] powerCost, double[] cooldown,
      Object[] description) throws IllegalArgumentException {
    super(id, type, pclass, levelCosts, powerCost, cooldown, description);
  }

  @Override
  public String getUserFriendlyName(int level) {
    return null;
  }

  @Override
  public String getActivationTime() {
    return null;
  }

  @Override
  public boolean onSkillUse(World world, CombatEntity ce, int level) {
    return false;
  }
}
