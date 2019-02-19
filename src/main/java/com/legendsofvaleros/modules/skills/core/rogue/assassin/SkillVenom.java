package com.legendsofvaleros.modules.skills.core.rogue.assassin;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillVenom extends Skill {
	public static final String ID = "venom";
	private static final int[] LEVELS = new int[] { 4 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 5 };
	private static final int[] TIME = new int[] { 30 };
	private static final int[] POISON = new int[] { 1 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Apply ",
			new EffectPart<Void>("PercentagePoison") { public void meta(int level, MetaEffectInstance<Void> meta) { meta.level = getEarliest(POISON, level); } },
			" to your weapon for ", new TimePart().seconds(TIME), "."
		};

	public SkillVenom() { super(ID, Type.SELF, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Venom"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}