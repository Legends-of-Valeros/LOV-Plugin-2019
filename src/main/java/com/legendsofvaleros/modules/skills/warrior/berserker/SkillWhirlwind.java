package com.legendsofvaleros.modules.skills.warrior.berserker;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

public class SkillWhirlwind extends Skill {
    public static final String ID = "whirlwind";
    private static final int[] LEVELS = new int[]{3, 1, 1, 1};
    private static final int[] COST = new int[]{5};
    private static final double[] COOLDOWN = new double[]{20};
    private static final int[] DAMAGE = new int[]{100, 110, 125, 150};
    private static final int[] RADIUS = new int[]{5};
    private static final Object[] DESCRIPTION = new Object[]{
            new WDPart(DAMAGE), " for all enemies within a ", new RadiusPart(RADIUS), "."
    };

    public SkillWhirlwind() {
        super(ID, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Whirlwind";
    }

    @Override
    public String getActivationTime() {
        return INSTANT;
    }

    @Override
    public boolean onSkillUse(World world, CombatEntity ce, int level) {
        Collection<LivingEntity> targets = getTargets(ce, getEarliest(RADIUS, level), LivingEntity.class);

        for (LivingEntity e : targets) {
            CombatEngine.getInstance().causePhysicalDamage(e, ce.getLivingEntity(), PhysicalType.OTHER,
                    Characters.inst().getCharacterConfig().getClassConfig(EntityClass.WARRIOR).getBaseMeleeDamage()
                            * getEarliest(DAMAGE, level) / 100D, null, false, true);
        }

        return targets.size() != 0;
    }
}