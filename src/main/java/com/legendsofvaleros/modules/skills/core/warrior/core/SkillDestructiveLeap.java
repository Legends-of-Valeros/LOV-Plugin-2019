package com.legendsofvaleros.modules.skills.core.warrior.core;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.skills.event.OnTouchGround;
import com.legendsofvaleros.util.VelocityUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

public class SkillDestructiveLeap extends Skill {
    public static final String ID = "destructiveleap";
    private static final int[] LEVELS = new int[]{-1, 2, 2};
    private static final int[] COST = new int[]{30};
    private static final double[] COOLDOWN = new double[]{60};
    private static final int[] RANGE = new int[]{10};
    private static final int[] DAMAGE = new int[]{60, 100, 150};
    private static final int[] RADIUS = new int[]{7};
    private static final Object[] DESCRIPTION = new Object[]{
            "Jump forward ", new RangePart(RANGE), ", dealing ",
            new PercentPart(DAMAGE), " of the attack power in a ", new RadiusPart(RADIUS), "."
    };

    public SkillDestructiveLeap() {
        super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Destructive Leap";
    }

    @Override
    public String getActivationTime() {
        return INSTANT;
    }

    @Override
    public boolean onSkillUse(World world, CombatEntity ce, int level) {
        Location to = ce.getLivingEntity().getLocation().clone();
        to.setPitch(0);
        to.add(to.getDirection().multiply(getEarliest(RANGE, level)));

        ce.getLivingEntity().setVelocity(VelocityUtil.calculateVelocity(VelocityUtil.PLAYER, ce.getLivingEntity().getLocation().toVector(), to.toVector(), 1));

        int area = getEarliest(RADIUS, level);
        OnTouchGround.call(ce.getLivingEntity(), (le) -> {
            for (CombatEntity e : validateTargets(ce, getNearbyEntities(le.getLocation(), area, 1, area))) {
                CombatEngine.getInstance().causePhysicalDamage((LivingEntity) e, ce.getLivingEntity(), PhysicalType.OTHER,
                        Characters.getInstance().getCharacterConfig().getClassConfig(EntityClass.WARRIOR).getBaseMeleeDamage()
                                * getEarliest(DAMAGE, level) / 100D, null, false, true);
            }
        });
        return true;
    }
}