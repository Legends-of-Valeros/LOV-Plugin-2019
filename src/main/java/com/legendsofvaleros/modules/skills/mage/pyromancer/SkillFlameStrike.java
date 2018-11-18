package com.legendsofvaleros.modules.skills.mage.pyromancer;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.skills.Skills;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillFlameStrike extends Skill {
    public static final String ID = "flamestrike";
    private static final int[] LEVELS = new int[]{4, 1};
    private static final int[] COST = new int[]{5};
    private static final double[] COOLDOWN = new double[]{35};
    private static final int[] RADIUS = new int[]{5};
    private static final int[] DAMAGE = new int[]{230, 350};
    private static final Object[] DESCRIPTION = new Object[]{
            "Summons a pillar of fire burning all enemies in a ",
            new RadiusPart(RADIUS), " for ", new WDPart(DAMAGE), "."
    };

    public SkillFlameStrike() {
        super(ID, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Flame Strike";
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
        final Location startLoc = ce.getLivingEntity().getLocation().clone();
        startLoc.setPitch(90F);
        startLoc.setYaw(0F);

        world.playSound(startLoc, "spell.fire.flame.scary.long", 1F, 1F);

        int radius = getEarliest(RADIUS, level);

        final long runInterval = 3;
        final long maxTime = 3 * 20 / runInterval;
        new BukkitRunnable() {
            int time = 0;

            @Override
            public void run() {
                for (double i = 0; i <= Math.PI * 2; i += Math.PI / 10D) {
                    double x = Math.cos(i + time) * radius;
                    double z = Math.sin(i + time) * radius;
                    world.spawnParticle(Particle.FLAME, startLoc.getX() + x, startLoc.getY() + i, startLoc.getZ() + z, 1, 0, 0, 0, 0.01);
                }

                time++;
                if (time >= maxTime) cancel();
            }
        }.runTaskTimerAsynchronously(LegendsOfValeros.getInstance(), 0L, runInterval);
        new BukkitRunnable() {
            int time = 0;

            @Override
            public void run() {
                for (double i = 0; i <= Math.PI * 2; i += Math.PI / 10D) {
                    double x = Math.cos(i - time) * radius / 3D;
                    double z = Math.sin(i - time) * radius / 3D;
                    world.spawnParticle(Particle.FLAME, startLoc.getX() + x, startLoc.getY() + i / 1.5D, startLoc.getZ() + z, 1, 0, 0, 0, 0.01);
                }

                time++;
                if (time >= maxTime) cancel();
            }
        }.runTaskTimerAsynchronously(LegendsOfValeros.getInstance(), 0L, runInterval);
        return true;
    }
}