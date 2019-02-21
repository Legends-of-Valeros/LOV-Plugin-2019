package com.legendsofvaleros.modules.skills.core.mage.pyromancer;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.skills.event.OnProjectile;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SmallFireball;

public class SkillFireball extends Skill {
    public static final String ID = "fireball";
    private static final int[] LEVELS = new int[]{2, 1, 1};
    private static final int[] COST = new int[]{2};
    private static final double[] COOLDOWN = new double[]{2};
    private static final int[] DAMAGE = new int[]{8, 16, 32};
    private static final Object[] DESCRIPTION = new Object[]{
            "Throws a fireball at the enemy, causing ", new DamagePart(DAMAGE), "."
    };

    public SkillFireball() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Fireball";
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

        SmallFireball f = OnProjectile.shoot(ce, 2, 20, SmallFireball.class, (ce1, entities) -> {
            if (entities.size() > 0)
                world.playSound(entities.get(0).getLocation(), "spell.fire.fireball.impact", 1F, 1F);

            for (LivingEntity e : entities)
                if (e != ce1.getLivingEntity())
                    CombatEngine.getInstance().causeSpellDamage(e, ce1.getLivingEntity(), SpellType.FIRE,
                            spellDamage, null, true, false);
        });
        f.setIsIncendiary(false);
        f.setYield(0F);
        ParticleFollow.follow(f, 20 * 5, Particle.FLAME, 2, 0, 0, 0, .05);
        return true;
    }
}