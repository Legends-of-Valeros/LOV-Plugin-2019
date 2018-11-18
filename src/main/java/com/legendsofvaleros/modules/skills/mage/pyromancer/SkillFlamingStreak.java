package com.legendsofvaleros.modules.skills.mage.pyromancer;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.skills.SkillUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

public class SkillFlamingStreak extends Skill {
    public static final String ID = "flamingstreak";
    private static final int[] LEVELS = new int[]{3, 1, 2};
    private static final int[] COST = new int[]{5};
    private static final double[] COOLDOWN = new double[]{60};
    private static final int[] DAMAGE = new int[]{200, 250, 300};
    private static final Object[] DESCRIPTION = new Object[]{
            "Blasts enemy with instant explosion for ",
            new WDPart(DAMAGE), ", can be casted while running."
    };

    public SkillFlamingStreak() {
        super(ID, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Flaming Streak";
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
        LivingEntity target = getTarget(ce, 15);
        if (target == null)
            return false;

        world.playSound(target.getLocation(), "spell.fire.fireball.impact", 1F, 1F);
        world.spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation(), 5, .5, .5, .5, .01);

        Location loc = ce.getLivingEntity().getEyeLocation().clone();
        for (int i = 0; i < 15; i++) {
            loc.add(loc.getDirection());
            world.spawnParticle(Particle.FLAME, loc, 2, .1, .1, .1, 0.01);

            if (target.getLocation().distance(loc) < 1.5D) break;
        }

        CombatEngine.getInstance().causeSpellDamage(target, ce.getLivingEntity(), SpellType.FIRE,
                SkillUtil.getSpellDamage(ce, SpellType.FIRE, Characters.inst().getCharacterConfig().getClassConfig(EntityClass.MAGE).getBaseMeleeDamage())
                        * getEarliest(DAMAGE, level) / 100D, null, false, true);

        return true;
    }
}