package com.legendsofvaleros.modules.skills.core.warrior.berserker;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
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
        super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION);
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
        Collection<CombatEntity> targets = validateTargets(ce, getTargets(ce, getEarliest(RADIUS, level), LivingEntity.class));

        for (CombatEntity e : targets) {
            CombatEngine.getInstance().causePhysicalDamage(e.getLivingEntity(), ce.getLivingEntity(), PhysicalType.OTHER,
                    Characters.getInstance().getCharacterConfig().getClassConfig(EntityClass.WARRIOR).getBaseMeleeDamage()
                            * getEarliest(DAMAGE, level) / 100D, null, false, true);
        }

        return targets.size() != 0;
    }
}