package com.legendsofvaleros.modules.skills.core.mage.pyromancer;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.skills.event.OnProjectile;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;

public class SkillPyrostrike extends Skill {
    public static final String ID = "pyrostrike";
    private static final int[] LEVELS = new int[]{4};
    private static final int[] COST = new int[]{2};
    private static final double[] COOLDOWN = new double[]{30};
    private static final int[] DAMAGE = new int[]{50};
    private static final Object[] DESCRIPTION = new Object[]{
            "Hurls a large fiery boulder towards the enemy which inflicts ",
            new DamagePart(DAMAGE), "."
    };

    public SkillPyrostrike() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Pyrostrike";
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
        world.playSound(ce.getLivingEntity().getLocation(), "spell.fire.fireball.throw", 1F, 1F);

        double spellDamage = getEarliest(DAMAGE, level);

        LargeFireball f = OnProjectile.shoot(ce, 2, 20, LargeFireball.class, (ce1, entities) -> {
            if (entities.size() > 0)
                world.playSound(entities.get(0).getLocation(), "spell.fire.fireball.impact.strong", 1F, 1F);

            for (LivingEntity e : entities)
                if (e != ce1.getLivingEntity())
                    CombatEngine.getInstance().causeSpellDamage(e, ce1.getLivingEntity(), SpellType.FIRE,
                            spellDamage, null, false, true);
        });
        f.setIsIncendiary(false);
        f.setYield(0F);
        ParticleFollow.follow(f, 20 * 5, Particle.FLAME, 4, 1, 1, 1, .05);
        return true;
    }
}