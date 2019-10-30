package com.legendsofvaleros.modules.skills.core.mage.core;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import org.bukkit.Particle;
import org.bukkit.World;

public class SkillFireSurge extends Skill {

  public static final String ID = "firesurge";
  private static final int[] LEVELS = new int[]{-1, 1, 2, 2, 3};
  private static final int[] COST = new int[]{1};
  private static final double[] COOLDOWN = new double[]{8};
  private static final int[] DAMAGE = new int[]{17, 43, 106, 266, 664};
  private static final Object[] DESCRIPTION = new Object[]{
      "Blasts the crosshair target with a fiery explosion of ",
      new DamagePart(DAMAGE), "."
  };

  public SkillFireSurge() {
    super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
  }

  @Override
  public String getUserFriendlyName(int level) {
    return "Fire Surge";
  }

  @Override
  public String getActivationTime() {
    return INSTANT;
  }

  @Override
  public boolean onSkillUse(World world, CombatEntity ce, int level) {
    CombatEntity target = validateTarget(ce, getTarget(ce, 15));
      if (target == null) {
          return false;
      }

    world.playSound(target.getLivingEntity().getLocation(), "spell.fire.flame.scary.short", 1F, 1F);
    world.spawnParticle(Particle.EXPLOSION_LARGE, target.getLivingEntity().getLocation(), 5, .5, .5, .5, .01);
    ParticleFollow.follow(target.getLivingEntity(), 20, Particle.FLAME, 3, .5, 2, .5, .01);

    CombatEngine.getInstance().causeSpellDamage(target.getLivingEntity(), ce.getLivingEntity(), SpellType.FIRE,
        getEarliest(DAMAGE, level),
        ce.getLivingEntity().getLocation(), false, true);

    return true;
  }
}