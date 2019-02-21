package com.legendsofvaleros.modules.skills.core.mage.cryomancer;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.skills.event.OnProjectile;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;

public class SkillIceCrystal extends Skill {
    public static final String ID = "icecrystal";
    private static final int[] LEVELS = new int[]{4, 1, 2};
    private static final int[] COST = new int[]{5};
    private static final double[] COOLDOWN = new double[]{20};
    private static final int[] DAMAGE = new int[]{30, 75, 188};
    private static final int[] RANGE = new int[]{10};
    private static final Object[] DESCRIPTION = new Object[]{
            "Ice shoots in shape of an ice crystal around you doing ",
            new WDPart(DAMAGE), " in a ",
            new RangePart(RANGE), "."
    };

    public SkillIceCrystal() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Ice Crystal";
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
        world.playSound(ce.getLivingEntity().getLocation(), "spell.ice.icebolt.impact.strong", .5F, .25F);

        double spellDamage = getEarliest(DAMAGE, level);

        Location start = ce.getLivingEntity().getLocation().add(0, 1, 0);
        for (float i = 0; i < 10; i++) {
            Location l = start.clone();
            l.setYaw(i * (360 / 10));
            l.add(l.getDirection().multiply(1.5D));
            Snowball s = OnProjectile.shoot(ce, l, 1, getEarliest(RANGE, level), Snowball.class, (ce1, entities) -> {
                for (LivingEntity e : entities)
                    if (e != ce1.getLivingEntity())
                        CombatEngine.getInstance().causeSpellDamage(e, ce1.getLivingEntity(), SpellType.ICE,
                                spellDamage, null, true, false);
            });
            s.setGravity(false);
            ParticleFollow.follow(s, 20 * 5, Particle.SNOW_SHOVEL, 2, 0, 0, 0);
        }

        return true;
    }
}