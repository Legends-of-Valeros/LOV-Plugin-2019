package com.legendsofvaleros.modules.skills.core.rogue.thief;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillStealth extends Skill {
	public static final String ID = "stealth";
	private static final int[] LEVELS = new int[] { 4 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] TIME = new int[] { 8 * 60 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Toggles sneaking, invisibility for ", new TimePart().seconds(TIME), ", cancelled by attacking or taking damage."
		};

	public SkillStealth() { super(ID, Type.BENEFICIAL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Stealth"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Characters.getInstance().getSkillEffectManager().getSkillEffect("Invisible").apply(ce.getLivingEntity(), ce.getLivingEntity(), getEarliest(TIME, level) * 1000);
		return true;
	}
}