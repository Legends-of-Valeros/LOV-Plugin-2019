package com.legendsofvaleros.modules.skills.core.mage.cryomancer;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SkillHailstorm extends Skill {
    public static final String ID = "hailstorm";
    private static final int[] LEVELS = new int[]{6, 1, 1, 2, 3};
    private static final int[] COST = new int[]{3};
    private static final double[] COOLDOWN = new double[]{45};
    private static final int[] RADIUS = new int[]{5};
    private static final int[] TIME = new int[]{10};
    private static final int[] DAMAGE = new int[]{50, 65, 80, 100, 150};
    private static final int[] FREEZE_CHANCE = new int[]{10};
    private static final int[] FREEZE_LEVEL = new int[]{10};
    private static final Object[] DESCRIPTION = new Object[]{
            "Ice rains from sky in a ",
            new RadiusPart(RADIUS), " for ",
            new TimePart().seconds(TIME), " which does ",
            new WDPart(DAMAGE), " and has a ",
            FREEZE_CHANCE, "% chance to cause ",
            new EffectPart<Double>("Freeze") {
                public void meta(int level, MetaEffectInstance<Double> meta) {
                    meta.level = getEarliest(FREEZE_LEVEL, level);
                }
            }
    };

    public SkillHailstorm() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Hailstorm";
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
        loc.setYaw(0F);

        world.playSound(loc, "spell.ice.icewind.soft", 1F, 1F);

        final int radius = getEarliest(RADIUS, level);
        final float chance = getEarliest(FREEZE_CHANCE, level) / 100F;

        final int runInterval = 2;
        final int maxTimes = getEarliest(TIME, level) * 20 / runInterval;
        new BukkitRunnable() {
            long runTimes = 0;
            Random rand = new Random();

            public void run() {
                if (runTimes % 4 == 0)
                    world.playSound(loc, "spell.ice.freeze", 1F, 1F);

                if (runTimes % 10 == 0) {
                    Random rand = new Random();
                    for (CombatEntity e : validateTargets(ce, getNearbyEntities(loc, radius, radius, radius))) {
                        if (rand.nextFloat() > chance) continue;
                        Characters.getInstance().getSkillEffectManager().getSkillEffect("Freeze").apply(e.getLivingEntity(), ce.getLivingEntity(), getEarliest(TIME, level));
                    }
                }

                for (int i = 0; i < 15; i++) {
                    Location c = loc.clone().add(rand.nextGaussian() * radius * (rand.nextBoolean() ? 1 : -1),
                            10 + rand.nextGaussian() * 2,
                            rand.nextGaussian() * radius * (rand.nextBoolean() ? 1 : -1));
                    world.spawnParticle(Particle.SNOWBALL, c, 3, 1, 1, 1, .01);
                }

                if (++runTimes >= maxTimes) cancel();
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 0L, runInterval);
        return true;
    }
}