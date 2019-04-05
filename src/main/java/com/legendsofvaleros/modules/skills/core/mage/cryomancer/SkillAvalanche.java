package com.legendsofvaleros.modules.skills.core.mage.cryomancer;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkillAvalanche extends Skill {
    public static final String ID = "avalanche";
    private static final int[] LEVELS = new int[]{3, 1};
    private static final int[] COST = new int[]{5};
    private static final double[] COOLDOWN = new double[]{25};
    private static final int[] RANGE = new int[]{8};
    private static final int[] THICKNESS = new int[]{4};
    private static final int[] DAMAGE = new int[]{40, 80};
    private static final Object[] DESCRIPTION = new Object[]{
            "Blasts a wall of snow and ice up to ",
            new RangePart(RANGE), " away from the mage dealing ",
            new DamagePart(DAMAGE), "."
    };

    static final Random rand = new Random();

    public SkillAvalanche() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Avalanche";
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
        final int damage = getEarliest(DAMAGE, level);

        final Location currLoc = ce.getLivingEntity().getLocation().clone();
        final Vector dir;
        {
            final Location eyeLoc = ce.getLivingEntity().getEyeLocation().clone();
            eyeLoc.setPitch(0F);
            dir = eyeLoc.getDirection();
        }

        List<CombatEntity> attacked = new ArrayList<>();

        world.playSound(currLoc, "spell.ice.icewind.strong", 1F, 1F);
        new BukkitRunnable() {
            int current = 0;

            public void run() {
                currLoc.add(dir);

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

                    Entity snowball = world.spawnEntity(loc.add(rand.nextFloat() / 10F - .05F, 0F, rand.nextFloat() / 10F - .05F), EntityType.SNOWBALL);
                    snowball.setVelocity(new Vector(rand.nextFloat() / 10F - .05F, .3, rand.nextFloat() / 10F - .05F));

                    perp.add(perpVec);
                }

                if (!found) {
                    cancel();
                    return;
                }

                for (CombatEntity e : validateTargets(ce, getNearbyEntities(perp, thickness / 2F, 2F, thickness / 2F))) {
                    if (!attacked.contains(e)) {
                        attacked.add(e);
                        CombatEngine.getInstance().causeSpellDamage(e.getLivingEntity(), ce.getLivingEntity(), SpellType.FIRE,
                                damage / 100D, ce.getLivingEntity().getLocation(), false, true);
                    }
                }

                current++;
                if (current > length)
                    cancel();
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 0L, 2L);
        return true;
    }
}