package com.legendsofvaleros.modules.skills.mage.core;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.skills.SkillUtil;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

public class SkillFireSurge extends Skill {
    public static final String ID = "firesurge";
    private static final int[] LEVELS = new int[]{-1, 1, 2, 2, 3};
    private static final int[] COST = new int[]{1};
    private static final double[] COOLDOWN = new double[]{12};
    private static final int[] DAMAGE = new int[]{300, 350, 400, 450, 500};
    private static final Object[] DESCRIPTION = new Object[]{
            "Blasts the crosshair target with a fiery explosion of ",
            new WDPart(DAMAGE), "."
    };

    public SkillFireSurge() {
        super(ID, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Fire Surge";
    }

    @Override
    public String getActivationTime() {
        return INSTANT;
    }

    @Override
    public boolean onSkillUse(World world, CombatEntity ce, int level) {
        LivingEntity target = getTarget(ce, 15);
        if (target == null)
            return false;

        world.playSound(target.getLocation(), "spell.fire.flame.scary.short", 1F, 1F);
        world.spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation(), 5, .5, .5, .5, .01);
        ParticleFollow.follow(target, 20, Particle.FLAME, 3, .5, 2, .5, .01);

        CombatEngine.getInstance().causeSpellDamage(target, ce.getLivingEntity(), SpellType.FIRE,
                SkillUtil.getSpellDamage(ce, SpellType.FIRE, Characters.inst().getCharacterConfig().getClassConfig(EntityClass.MAGE).getBaseMeleeDamage())
                        * getEarliest(DAMAGE, level) / 100D,
                ce.getLivingEntity().getLocation(), false, true);

        return true;
    }
}