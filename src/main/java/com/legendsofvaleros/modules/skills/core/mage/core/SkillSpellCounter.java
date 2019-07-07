package com.legendsofvaleros.modules.skills.core.mage.core;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;
import com.legendsofvaleros.modules.mobs.core.Mob;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SkillSpellCounter extends Skill {
    public static final String ID = "spellcounter";
    private static final int[] LEVELS = new int[]{-1, 1, 1, 2, 3};
    private static final int[] COST = new int[]{2};
    private static final double[] COOLDOWN = new double[]{24};
    private static final int[] TIME = new int[]{4, 5, 6, 7, 8};
    private static final Object[] DESCRIPTION = new Object[]{
            "Prevents enemy spellcasting for ", new TimePart().seconds(TIME), "."
    };

    public SkillSpellCounter() {
        super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION);
    }

    @Override
    public String getUserFriendlyName(int level) {
        return "Spell Counter";
    }

    @Override
    public String getActivationTime() {
        return INSTANT;
    }

    @Override
    public boolean onSkillUse(World world, CombatEntity ce, int level) {
        CombatEntity target = validateTarget(ce, getTarget(ce, 16D));
        if (target == null) return false;

        EntityClass clazz;

        if (target.isPlayer()) {
            clazz = Characters.getPlayerCharacter((Player) target.getLivingEntity()).getPlayerClass();
        } else {
            Mob.Instance mob = Mob.Instance.get(target.getLivingEntity());
            clazz = mob.mob.getEntityClass();
        }

        if (clazz != EntityClass.MAGE) return false;

        world.playSound(ce.getLivingEntity().getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, .5F, .5F);

        target.getStatusEffects().addStatusEffect(StatusEffectType.SILENCE, getEarliest(TIME, level));

        return true;
    }
}