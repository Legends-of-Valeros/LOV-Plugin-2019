package com.legendsofvaleros.modules.skills.mage.pyromancer;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillFlameShield extends Skill {
    public static final String ID = "flameshield";
    private static final int[] LEVELS = new int[]{4};
    private static final int[] COST = new int[]{3};
    private static final double[] COOLDOWN = new double[]{150};
    private static final int[] CHANCE = new int[]{50};
    private static final int[] REFLECTION = new int[]{30};
    private static final int[] TIME = new int[]{10};
    private static final Object[] DESCRIPTION = new Object[]{
            "Flame shield around mage, enemy attacks have ", CHANCE, "% chance taking ",
            new WDPart(REFLECTION), " for ",
            new TimePart().seconds(TIME), "."
    };

    public SkillFlameShield() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Flame Shield";
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
        final long runInterval = 3;
        final long maxTime = getEarliest(TIME, level) * 20 / runInterval;
        new BukkitRunnable() {
            int time = 0;

            @Override
            public void run() {
                Location loc = ce.getLivingEntity().getLocation();

                if (time % 6 == 0)
                    world.playSound(loc, "spell.fire.burn", 1F, 1F);

                for (double i = 0; i <= Math.PI * 2; i += Math.PI / 10D) {
                    double x = Math.cos(i - time) * 1.2D / ((i + 1D) / 2D);
                    double z = Math.sin(i - time) * 1.2D / ((i + 1D) / 2D);
                    world.spawnParticle(Particle.FLAME, loc.getX() + x, loc.getY() + i / 2D, loc.getZ() + z, 1, 0, 0, 0, 0.01);
                }

                time++;
                if (time >= maxTime) cancel();
            }
        }.runTaskTimerAsynchronously(LegendsOfValeros.getInstance(), 0L, runInterval);
        return true;
    }
}