package com.legendsofvaleros.modules.skills.mage.pyromancer;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SkillWalkOfFire extends Skill {
    public static final String ID = "walkoffire";
    private static final int[] LEVELS = new int[]{3, 1, 2, 3};
    private static final int[] COST = new int[]{10};
    private static final double[] COOLDOWN = new double[]{60};
    private static final int[] RANGE = new int[]{8};
    private static final int[] THICKNESS = new int[]{3};
    private static final int[] DAMAGE = new int[]{90, 150, 200, 300};
    private static final Object[] DESCRIPTION = new Object[]{
            "Trail of fire ",
            new RangePart(RANGE), " long and ",
            new RangePart(THICKNESS), " thick in front of the mage dealing ",
            new WDPart(DAMAGE), "."
    };

    public SkillWalkOfFire() {
        super(ID, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Walk of Fire";
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
        final int length = getEarliest(RANGE, level);
        final int thickness = getEarliest(THICKNESS, level);
        final float damage = getEarliest(DAMAGE, level);

        final Location currLoc = ce.getLivingEntity().getLocation().clone();
        final Vector dir;
        {
            final Location eyeLoc = ce.getLivingEntity().getEyeLocation().clone();
            eyeLoc.setPitch(0F);
            dir = eyeLoc.getDirection();
        }

        new BukkitRunnable() {
            int current = 0;

            public void run() {
                currLoc.add(dir);

                if (current % 4 == 0)
                    world.playSound(currLoc, "spell.fire.burn", .5F, 2F);

                Location perp = currLoc.clone().add(dir);
                perp.setYaw(perp.getYaw() + 90F);
                Vector perpVec = perp.getDirection();
                perp.subtract(perpVec.clone().multiply(thickness / 2F));
                //perp.add(.5F, 0, .5F);

                boolean found = false;
                Location loc;
                for (int i = 0; i < thickness; i++) {
                    loc = null;

                    for (int j = -2; j < 2; j++)
                        if (!world.getBlockAt(perp.getBlockX(), perp.getBlockY() + j - 1, perp.getBlockZ()).getType().isTransparent()
                                && world.getBlockAt(perp.getBlockX(), perp.getBlockY() + j, perp.getBlockZ()).getType().isTransparent()) {
                            loc = perp.clone().add(0, j, 0);
                            break;
                        }

                    if (loc == null)
                        continue;
                    found = true;

                    spawnFlame(world, loc, ce, damage);

                    perp.add(perpVec);
                }

                if (!found) {
                    cancel();
                    return;
                }

                if (++current > length)
                    cancel();
            }
        }.runTaskTimerAsynchronously(LegendsOfValeros.getInstance(), 0L, 2L);
        return true;
    }

    private void spawnFlame(World world, Location loc, CombatEntity ce, double damage) {
        new BukkitRunnable() {
            int runtime = 0;

            public void run() {
                if (runtime % 20 == 0)
                    world.playSound(loc, "spell.fire.burn", .5F, 2F);

                if (runtime == 0)
                    world.spawnParticle(Particle.LAVA, loc, 2, .5, .2, .5, 0.01);
                if (runtime % 2 == 0)
                    world.spawnParticle(Particle.FLAME, loc, 1, .5, .2, .5, 0.01);

                if (runtime % 2 == 0)
                    for (CombatEntity e : validateTargets(ce, getNearbyEntities(loc, .5, .5, .5), false)) {
                        CombatEngine.getInstance().causeSpellDamage(e.getLivingEntity(), ce.getLivingEntity(), SpellType.FIRE,
                                damage / 20D / 100D, null, false, true);
                    }

                if (++runtime > 40)
                    cancel();
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 0L, 1L);
    }
}