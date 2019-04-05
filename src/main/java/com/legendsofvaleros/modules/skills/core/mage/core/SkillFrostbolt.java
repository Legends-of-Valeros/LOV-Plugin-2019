package com.legendsofvaleros.modules.skills.core.mage.core;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class SkillFrostbolt extends Skill {
    public static final String ID = "frostbolt";
    private static final int[] LEVELS = new int[]{-1, 1, 2, 3, 4};
    private static final int[] COST = new int[]{2};
    private static final double[] COOLDOWN = new double[]{2};
    private static final int[] RANGE = new int[]{8};
    private static final int[] DAMAGE = new int[]{5, 10, 20, 40, 80};
    private static final int[] SLOW = new int[]{50};
    private static final int[] TIME = new int[]{2};
    private static final Object[] DESCRIPTION = new Object[]{
            "Shoots a bolt of ice ", new RangePart(RANGE),
            ", dealing ", new DamagePart(DAMAGE), ", slows target ",
            new PercentPart(SLOW), " for ", new TimePart().seconds(TIME), "."
    };

    public SkillFrostbolt() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Frostbolt";
    }

    @Override
    public String getActivationTime() {
        return INSTANT;
    }

    @Override
    public boolean onSkillUse(World world, CombatEntity ce, int level) {
        CombatEntity target = validateTarget(ce, getTarget(ce, 15));

        world.playSound(ce.getLivingEntity().getLocation(), "spell.ice.icewind.soft", .5F, .5F);

        Location loc = ce.getLivingEntity().getEyeLocation().clone();
        for (int i = 0; i < getEarliest(RANGE, level); i++) {
            loc.add(loc.getDirection());
            world.spawnParticle(Particle.SNOW_SHOVEL, loc, 2, .1, .1, .1, 0.01);
        }

        if (target != null) {
            CombatEngine.getInstance().causeSpellDamage(target.getLivingEntity(), ce.getLivingEntity(), SpellType.ICE,
                    getEarliest(DAMAGE, level), null, false, true);

            target.getStats().newStatModifierBuilder(Stat.SPEED)
                    .setDuration(getEarliest(TIME, level) * 20)
                    .setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
                    .setValue(getEarliest(SLOW, level) / 100D)
                    .build();
        }

        return true;
    }
}