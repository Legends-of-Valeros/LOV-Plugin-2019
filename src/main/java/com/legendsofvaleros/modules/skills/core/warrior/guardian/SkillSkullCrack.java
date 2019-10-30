package com.legendsofvaleros.modules.skills.core.warrior.guardian;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.classes.skills.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillSkullCrack extends Skill {
	public static final String ID = "skullcrack";
	private static final int[] LEVELS = new int[] { 3 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] CONFUSE_LEVEL = new int[] { 10 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Whack your enemy on the head, causing ", new EffectPart<Double>("Confuse") {
				public void meta(int level, MetaEffectInstance<Double> meta) {
					meta.level = getEarliest(CONFUSE_LEVEL, level);
				}
			}, "."
		};

	public SkillSkullCrack() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Skull Crack"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> Characters.getInstance().getSkillEffectManager().getSkillEffect("Confuse").apply(e.getAttacker().getLivingEntity(), ce.getLivingEntity(), getEarliest(CONFUSE_LEVEL, level)));
		return true;
	}
}