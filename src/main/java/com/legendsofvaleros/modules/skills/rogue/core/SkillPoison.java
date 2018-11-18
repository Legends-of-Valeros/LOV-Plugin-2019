package com.legendsofvaleros.modules.skills.rogue.core;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.characters.skilleffect.effects.PercentagePoison;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.characters.skilleffect.effects.PercentagePoison;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.characters.skilleffect.effects.PercentagePoison;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillPoison extends Skill {
	public static final String ID = "poison";
	private static final int[] LEVELS = new int[] { -1, 2, 3 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 180 };
	private static final int[] WEAPON_TIME = new int[] { 10 };
	private static final int[] POISON = new int[] { (int)(2 / PercentagePoison.PERCENT_PER_LEVEL),
														(int)(2.5 / PercentagePoison.PERCENT_PER_LEVEL),
														(int)(3 / PercentagePoison.PERCENT_PER_LEVEL) };
	//private static final int[] POISON_TIME = new int[] { 8 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Apply ",
			new EffectPart<Void>("PercentagePoison") { public void meta(int level, MetaEffectInstance<Void> meta) { meta.level = getEarliest(POISON, level); } },
			" to your weapon for ", new TimePart().seconds(WEAPON_TIME), "."
		};

	public SkillPoison() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Poison"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		/*NextAttack.on(pc.getPlayer().getUniqueId(), new NextAttackListener() {
			@Override
			public double run(Player p, LivingEntity attacked, double dmg) {
				Characters.getInstance().getSkillEffectManager().getSkillEffect("PercentagePoison").apply(attacked, p, 1, 1000 * getEarliest(POISON_TIME, level));
				return dmg;
			}
		});*/
		return true;
	}
}