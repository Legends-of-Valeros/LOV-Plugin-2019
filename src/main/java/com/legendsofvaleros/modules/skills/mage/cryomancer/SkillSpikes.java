package com.legendsofvaleros.modules.skills.mage.cryomancer;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkillSpikes extends Skill {
    public static final String ID = "spikes";
    private static final int[] LEVELS = new int[]{6, 1, 2, 3};
    private static final int[] COST = new int[]{15};
    private static final double[] COOLDOWN = new double[]{240};
    private static final int[] RADIUS = new int[]{10};
    private static final int[] DAMAGE = new int[]{500, 550, 600, 700};
    private static final Object[] DESCRIPTION = new Object[]{
            "Ice spikes rise from the ground in a ",
            new RadiusPart(RADIUS), ", damaging enemies hit by ",
            new WDPart(DAMAGE), "."
    };

    public SkillSpikes() {
        super(ID, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Spikes";
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
        final Location loc = ce.getLivingEntity().getLocation().clone().add(0, 2, 0);
        for (int i = loc.getBlockY(); i > 0; i--) {
            if (world.getBlockAt(loc).getType().isTransparent()
                    && !world.getBlockAt(loc.subtract(0, 1, 0)).getType().isTransparent()) {
                loc.add(0, 1, 0);
                break;
            }
        }

        Random rand = new Random();
        List<Block> successes = new ArrayList<>();

        Location spikeLoc;
        Block b;

        final int radius = getEarliest(RADIUS, level);
        for (int i = 0; i < 8; i++) {
            spikeLoc = loc.clone().add(rand.nextGaussian() * radius * (rand.nextBoolean() ? .5 : -.5),
                    0,
                    rand.nextGaussian() * radius * (rand.nextBoolean() ? .5 : -.5));
            world.spawnParticle(Particle.EXPLOSION_LARGE, spikeLoc, 1, .5, .5, .5, 0.01);
            world.playSound(spikeLoc, "spell.ice.icebolt.impact.strong", .5F, 1F);

            b = world.getBlockAt(spikeLoc);
            if (b.getType() != Material.AIR) continue;
            b.setType(Material.THIN_GLASS);
            successes.add(b);

            spikeLoc.add(0, 1, 0);
            b = world.getBlockAt(spikeLoc);
            if (b.getType() != Material.AIR) continue;
            b.setType(Material.THIN_GLASS);
            successes.add(b);

            if (rand.nextBoolean()) {
                spikeLoc.add(0, 1, 0);
                b = world.getBlockAt(spikeLoc);
                if (b.getType() != Material.AIR) continue;
                b.setType(Material.THIN_GLASS);
                successes.add(b);
            }
        }

        if (successes.size() == 0) return false;

        new BukkitRunnable() {
            @Override
            public void run() {
                Block b = successes.remove(successes.size() - 1);

                b.setType(Material.AIR);

                world.spawnParticle(Particle.BLOCK_CRACK, b.getLocation(), 3, .5, .5, .5, 0.01, new MaterialData(Material.ICE));

                if (successes.size() == 0)
                    cancel();
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 40L, 10L);
        return true;
    }
}