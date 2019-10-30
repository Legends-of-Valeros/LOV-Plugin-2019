package com.legendsofvaleros.modules.skills.core.mage.core;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.classes.skills.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.Sound;
import org.bukkit.World;

public class SkillPolymorph extends Skill {

  public static final String ID = "polymorph";
  private static final int[] LEVELS = new int[]{-1};
  private static final int[] COST = new int[]{2};
  private static final double[] COOLDOWN = new double[]{120};
  private static final int[] MORPH_LEVEL = new int[]{10};
  private static final Object[] DESCRIPTION = new Object[]{
      "Apply ", new EffectPart<Double>("Polymorph") {
    public void meta(int level, MetaEffectInstance<Double> meta) {
      meta.level = getEarliest(MORPH_LEVEL, level);
    }
  }, " to target, cancelled when receiving damage, heals enemy 200% rate."
  };

  public SkillPolymorph() {
    super(ID, Type.NEUTRAL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
  }

  @Override
  public String getUserFriendlyName(int level) {
    return "Polymorph";
  }

  @Override
  public String getActivationTime() {
    return INSTANT;
  }

  @Override
  public boolean onSkillUse(World world, CombatEntity ce, int level) {
    CombatEntity target = validateTarget(ce, getTarget(ce, 16D));
		if (target == null) {
			return false;
		}

    world.playSound(target.getLivingEntity().getLocation(), "misc.resurrect", 1F, 1F);
    world.playSound(target.getLivingEntity().getLocation(), Sound.ENTITY_SHEEP_AMBIENT, .5F, 1F);

    Characters.getInstance().getSkillEffectManager().getSkillEffect("Polymorph")
        .apply(target.getLivingEntity(), ce.getLivingEntity(), getEarliest(MORPH_LEVEL, level));
    target.getStats().newStatModifierBuilder(Stat.HEALTH_REGEN)
        .setDuration(getEarliest(MORPH_LEVEL, level) * 20)
        .setValue(2)
        .setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
        .build();

    return true;
  }
}