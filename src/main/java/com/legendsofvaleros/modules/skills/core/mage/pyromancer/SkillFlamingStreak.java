package com.legendsofvaleros.modules.skills.core.mage.pyromancer;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class SkillFlamingStreak extends Skill {
    public static final String ID = "flamingstreak";
    private static final int[] LEVELS = new int[]{3, 1, 2};
    private static final int[] COST = new int[]{5};
    private static final double[] COOLDOWN = new double[]{11};
    private static final int[] DAMAGE = new int[]{20, 50, 125};
    private static final Object[] DESCRIPTION = new Object[]{
            "Blasts enemy with instant explosion for ",
            new DamagePart(DAMAGE), ", can be casted while running."
    };

    public SkillFlamingStreak() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Flaming Streak";
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
        CombatEntity target = validateTarget(ce, getTarget(ce, 15));
        if (target == null) return false;

        world.playSound(target.getLivingEntity().getLocation(), "spell.fire.fireball.impact", 1F, 1F);
        world.spawnParticle(Particle.EXPLOSION_LARGE, target.getLivingEntity().getLocation(), 5, .5, .5, .5, .01);

        Location loc = ce.getLivingEntity().getEyeLocation().clone();
        for (int i = 0; i < 15; i++) {
            loc.add(loc.getDirection());
            world.spawnParticle(Particle.FLAME, loc, 2, .1, .1, .1, 0.01);

            if (target.getLivingEntity().getLocation().distance(loc) < 1.5D) break;
        }

        CombatEngine.getInstance().causeSpellDamage(target.getLivingEntity(), ce.getLivingEntity(), SpellType.FIRE,
                getEarliest(DAMAGE, level), null, false, true);

        return true;
    }
}