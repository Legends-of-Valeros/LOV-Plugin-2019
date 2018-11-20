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
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SkillFrostShield extends Skill {
    public static final String ID = "frostshield";
    private static final int[] LEVELS = new int[]{3};
    private static final int[] COST = new int[]{5};
    private static final double[] COOLDOWN = new double[]{25};
    private static final int[] RANGE = new int[]{5};
    private static final int[] TIME = new int[]{5};
    private static final Object[] DESCRIPTION = new Object[]{
            "A shield of ice blocks is formed ",
            new RangePart(RANGE), " away from the mage, blocking all damage for ",
            new TimePart().seconds(TIME), "."
    };

    public SkillFrostShield() {
        super(ID, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Frost Shield";
    }

    @Override
    public String getActivationTime() {
        return INSTANT;
    }

    @Override
    public boolean doesRequireFocus() {
        return true;
    }

    private final int[] AXIS_ROW = new int[]{
            -3, -1,
            -3, -2,
            -2, -3,

            -1, -4,
            0, -4,
            1, -4,

            2, -3,
            3, -2,
            3, -1
    };
    private final int[][] AXIS = new int[][]{
            AXIS_ROW, AXIS_ROW, AXIS_ROW,
            new int[]{
                    -3, 0,
                    -3, -1,
                    -2, -2,

                    -2, -3,
                    -1, -3,
                    0, -3,
                    1, -3,
                    2, -3,

                    2, -2,
                    3, -1,
                    3, 0
            }, new int[]{
            -2, 0,
            -1, 0,
            -1, 0,
            0, 0,
            1, 0,
            2, 0,

            -2, -1,
            -1, -1,
            -1, -1,
            0, -1,
            1, -1,
            2, -1,

            -2, -2,
            -1, -2,
            -1, -2,
            0, -2,
            1, -2,
            2, -2,

            -1, -3,
            -1, -3,
            0, -3,
            1, -3
    }
    };
    private final int[] DIAGONAL_ROW = new int[]{
            1, -3,
            0, -4,
            -1, -4,
            -2, -4,
            -3, -3,
            -4, -2,
            -4, -1,
            -4, 0,
            -3, 1
    };
    private final int[][] DIAGONAL = new int[][]{
            DIAGONAL_ROW, DIAGONAL_ROW, DIAGONAL_ROW,
            new int[]{
                    2, -2,
                    1, -3,
                    0, -3,
                    -1, -4,
                    -2, -3,
                    -3, -2,
                    -4, -1,
                    -3, 0,
                    -3, 1,
                    -2, 2
            }, new int[]{
            -1, 0,
            -2, 0,
            -2, 1,
            -3, 0,
            0, -1,
            -1, -1,
            -2, -1,
            -3, -1,
            -4, -1,
            1, -2,
            0, -2,
            -1, -2,
            -2, -2,
            0, -3,
            -1, -3
    }
    };

    @Override
    public boolean onSkillUse(World world, CombatEntity ce, int level) {
        List<Block> successes = new ArrayList<>();

        Location eyeLoc = ce.getLivingEntity().getEyeLocation().clone();
        eyeLoc.setPitch(0F);
        Vector dir = eyeLoc.getDirection();

        int xMul = NumberConversions.round(dir.getX());
        int zMul = NumberConversions.round(dir.getZ());

        Location spawnLoc = ce.getLivingEntity().getLocation().clone().add(dir.multiply(getEarliest(RANGE, level) - 2));

        world.playSound(spawnLoc, "spell.ice.iceblast.freeze", 1F, 1F);

        Location l;
        Block b;
        if (zMul != 0 && xMul != 0) {
            for (int y = 0; y < DIAGONAL.length; y++) {
                final List<Block> row = new ArrayList<>();
                for (int i = 0; i < DIAGONAL[y].length; i += 2) {
                    l = spawnLoc.clone();
                    if (xMul == 1 && zMul == 1)
                        l.add(-DIAGONAL[y][i], y, -DIAGONAL[y][i + 1]);
                    else if (xMul == -1 && zMul == 1)
                        l.add(DIAGONAL[y][i], y, -DIAGONAL[y][i + 1]);
                    else if (xMul == 1 && zMul == -1)
                        l.add(-DIAGONAL[y][i + 1], y, DIAGONAL[y][i]);
                    else if (xMul == -1 && zMul == -1)
                        l.add(DIAGONAL[y][i + 1], y, DIAGONAL[y][i]);

                    b = world.getBlockAt(l);
                    if (b.getType() != Material.AIR) continue;
                    row.add(b);
                }
                successes.addAll(row);

                spawnRow(world, row, y);
            }
        } else {
            for (int y = 0; y < AXIS.length; y++) {
                final List<Block> row = new ArrayList<>();
                for (int i = 0; i < AXIS[y].length; i += 2) {
                    l = spawnLoc.clone();
                    if (xMul == 0 && zMul == 1)
                        l.add(AXIS[y][i], y, -AXIS[y][i + 1]);
                    if (xMul == 0 && zMul == -1)
                        l.add(AXIS[y][i], y, AXIS[y][i + 1]);
                    else if (xMul == -1 && zMul == 0)
                        l.add(AXIS[y][i + 1], y, AXIS[y][i]);
                    else if (xMul == 1 && zMul == 0)
                        l.add(-AXIS[y][i + 1], y, AXIS[y][i]);
                    b = world.getBlockAt(l);
                    if (b.getType() != Material.AIR) continue;
                    row.add(b);
                }
                successes.addAll(row);

                spawnRow(world, row, y);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                world.playSound(spawnLoc, "spell.ice.iceshield.break", .25F, 1F);

                for (Block b : successes) {
                    b.setType(Material.AIR);

                    world.spawnParticle(Particle.BLOCK_CRACK, b.getLocation(), 3, .5, .5, .5, 0.01, new MaterialData(Material.ICE));
                }
            }
        }.runTaskLater(LegendsOfValeros.getInstance(), getEarliest(TIME, level) * 20 + 10L);
        return true;
    }

    private void spawnRow(World world, List<Block> row, int y) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block b : row) {
                    b.setType(Material.ICE);

                    world.spawnParticle(Particle.EXPLOSION_LARGE, b.getLocation(), 1, .5, .5, .5, 0.01);
                }
            }
        }.runTaskLater(LegendsOfValeros.getInstance(), y * 2L);
    }
}