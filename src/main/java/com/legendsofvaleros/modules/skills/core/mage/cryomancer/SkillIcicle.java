package com.legendsofvaleros.modules.skills.core.mage.cryomancer;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.skills.event.OnProjectile;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class SkillIcicle extends Skill {

  public static final String ID = "icicle";
  private static final int[] LEVELS = new int[]{2, 1, 1};
  private static final int[] COST = new int[]{2};
  private static final double[] COOLDOWN = new double[]{7};
  private static final int[] RANGE = new int[]{25};
  private static final int[] DAMAGE = new int[]{15, 38, 94};
  private static final int[] KNOCKBACK_BLOCKS = new int[]{1, 2, 3};
  private static final Object[] DESCRIPTION = new Object[]{
      "Shoots an icicle ", new RangePart(RANGE), ", dealing ",
      new DamagePart(DAMAGE), " and knocking targets back ",
      new RangePart(KNOCKBACK_BLOCKS), "."
  };

  public SkillIcicle() {
    super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
  }

  @Override
  public String getUserFriendlyName(int level) {
    return "Icicle";
  }

  @Override
  public String getActivationTime() {
    return INSTANT;
  }

  @Override
  public boolean doesRequireFocus() {
    return true;
  }

  @Override
  public boolean onSkillUse(World world, CombatEntity ce, int level) {
    world.playSound(ce.getLivingEntity().getLocation(), "spell.ice.icebolt.impact.soft", .5F, .5F);

    double spellDamage = getEarliest(DAMAGE, level);

    final Vector knockback = ce.getLivingEntity().getLocation().getDirection().multiply(getEarliest(KNOCKBACK_BLOCKS, level));
    Snowball s = OnProjectile.shoot(ce, 1, getEarliest(RANGE, level), Snowball.class, (ce1, entities) -> {
        for (LivingEntity e : entities) {
            if (e != ce1.getLivingEntity()) {
                CombatEngine.getInstance().causeSpellDamage(e, ce1.getLivingEntity(), SpellType.ICE,
                    spellDamage, null, true, false);
                e.getLocation().add(knockback);
            }
        }
    });
    s.setGravity(false);
    ParticleFollow.follow(s, 20 * 5, Particle.SNOW_SHOVEL, 2, 0, 0, 0);
    return true;
  }
}