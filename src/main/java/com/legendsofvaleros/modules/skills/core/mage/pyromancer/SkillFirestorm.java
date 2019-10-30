package com.legendsofvaleros.modules.skills.core.mage.pyromancer;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.skills.event.OnProjectile;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.SmallFireball;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SkillFirestorm extends Skill {
    public static final String ID = "firestorm";
    private static final int[] LEVELS = new int[]{6, 1, 1, 2, 3};
    private static final int[] COST = new int[]{3};
    private static final double[] COOLDOWN = new double[]{45};
    private static final int[] RADIUS = new int[]{5};
    private static final int[] WAVE_INTERVAL = new int[]{2};
    private static final int[] TIME = new int[]{10};
    private static final int[] DAMAGE = new int[]{70, 85, 100, 130, 200};
    private static final Object[] DESCRIPTION = new Object[]{
            "Fire rains from sky every ",
            new TimePart().seconds(WAVE_INTERVAL), " in a ",
            new RadiusPart(RADIUS), ", doing ", new WDPart(DAMAGE), ", for ",
            new TimePart().seconds(TIME), "."
    };

    public SkillFirestorm() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Firestorm";
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
        final Location loc = ce.getLivingEntity().getLocation().clone();
        loc.setPitch(70F);

        final int times = getEarliest(TIME, level);
        final int radius = getEarliest(RADIUS, level);

        final int perSecond = 2;
        new BukkitRunnable() {
            long runTimes = 0;
            Random rand = new Random();

            public void run() {
                if (runTimes % (getEarliest(WAVE_INTERVAL, level) * perSecond) == 0) {
                    world.playSound(loc, "spell.fire.burn", 1F, 1F);

                    for (int i = 0; i < 3; i++) {
                        Location c = loc.clone().add(rand.nextGaussian() * radius * (rand.nextBoolean() ? 1 : -1),
                                10 + rand.nextGaussian() * 2,
                                rand.nextGaussian() * radius * (rand.nextBoolean() ? 1 : -1));
                        LargeFireball f = OnProjectile.shoot(ce, c, 10, LargeFireball.class, null);
                        f.setIsIncendiary(false);
                        f.setYield(0F);
                        ParticleFollow.follow(f, 20 * 5, Particle.FLAME, 3, .5, .5, .5, .05);

                        world.spawnParticle(Particle.EXPLOSION_LARGE, c, 10, .5, .5, .5, 0.01);
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        Location c = loc.clone().add(rand.nextGaussian() * radius * (rand.nextBoolean() ? 1 : -1),
                                8 + rand.nextGaussian() * 2,
                                rand.nextGaussian() * radius * (rand.nextBoolean() ? 1 : -1));
                        SmallFireball f = OnProjectile.shoot(ce, c, 15, SmallFireball.class, null);
                        f.setIsIncendiary(false);
                        f.setYield(0F);
                        ParticleFollow.follow(f, 20 * 10, Particle.FLAME, 2, 0, 0, 0, .05);

                        world.spawnParticle(Particle.SMOKE_LARGE, c, 10, .25, .25, .25, 0.05);
                    }
                }

                if (++runTimes >= times * perSecond) cancel();
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 0L, 20L / perSecond);
        return true;
    }
}