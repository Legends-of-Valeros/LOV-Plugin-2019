package com.legendsofvaleros.modules.skills.core.mage.cryomancer;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.classes.skills.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillIcyBlast extends Skill {
    public static final String ID = "icyblast";
    private static final int[] LEVELS = new int[]{4};
    private static final int[] COST = new int[]{8};
    private static final double[] COOLDOWN = new double[]{120};
    private static final int[] RADIUS = new int[]{10};
    private static final int[] FREEZE_LEVEL = new int[]{4};
    private static final Object[] DESCRIPTION = new Object[]{
            "A wave of ice originating from the cryomancer in a ",
            new RadiusPart(RADIUS), " causing ", new EffectPart<Double>("Freeze") {
        public void meta(int level, MetaEffectInstance<Double> meta) {
            meta.level = getEarliest(FREEZE_LEVEL, level);
        }
    }, "."
    };

    public SkillIcyBlast() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Icy Blast";
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
        world.playSound(ce.getLivingEntity().getLocation(), "spell.ice.icewind.strong", 1F, 1F);

        int radius = getEarliest(RADIUS, level);
        for (CombatEntity e : validateTargets(ce, getTargets(ce, radius, LivingEntity.class))) {
            if (e.getLivingEntity() instanceof Player) {
                if (!Characters.isPlayerCharacterLoaded((Player) e))
                    continue;
                ((Player)ce.getLivingEntity()).playSound(ce.getLivingEntity().getLocation(), "spell.ice.freeze", 1F, 1F);
            }
            Characters.getInstance().getSkillEffectManager().getSkillEffect("Freeze").apply(e.getLivingEntity(), ce.getLivingEntity(), getEarliest(FREEZE_LEVEL, level));
        }

        Location start = ce.getLivingEntity().getLocation().clone();
        start.setPitch(0);
        start.add(0, .1, 0);

        int perSecond = 10;
        int times = getEarliest(FREEZE_LEVEL, level);
        new BukkitRunnable() {
            int runTimes = 0;

            @Override
            public void run() {
                float i = ((runTimes + 1) % 10) / 10F * radius;

                for (float j = 0; j < 20; j++) {
                    Location l = start.clone();
                    l.setYaw(j * (360 / 20));
                    l.add(l.getDirection().multiply(i));

                    world.spawnParticle(Particle.SNOW_SHOVEL, l, 3, .2, 0, .2, 0.01);
                }

                if (++runTimes >= times * perSecond) cancel();
            }
        }.runTaskTimerAsynchronously(LegendsOfValeros.getInstance(), 0L, 20L / perSecond);
        return true;
    }
}