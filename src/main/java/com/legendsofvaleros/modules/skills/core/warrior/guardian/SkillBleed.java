package com.legendsofvaleros.modules.skills.core.warrior.guardian;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.classes.skills.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillBleed extends Skill {
	public static final String ID = "bleed";
	private static final int[] LEVELS = new int[] { 5, 1, 2 };
	private static final int[] COST = new int[] { 20 };
	private static final double[] COOLDOWN = new double[] { 45 };
	private static final int[] DAMAGE = new int[] { 300, 350, 400 };
	private static final int[] BLEED = new int[] { 2, 3, 5 };
	private static final int[] BLEED_TIME = new int[] { 8 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Does ", new WDPart(DAMAGE), " + ",
			new EffectPart<Void>("Bleed") { public void meta(int level, MetaEffectInstance<Void> meta) { meta.level = getEarliest(BLEED, level); } },
			" for ", new TimePart().seconds(BLEED_TIME), "."
		};

	public SkillBleed() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Bleed"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> {
			Characters.getInstance().getSkillEffectManager().getSkillEffect("Bleed").apply(e.getAttacker().getLivingEntity(), ce.getLivingEntity(), getEarliest(BLEED, level), getEarliest(BLEED_TIME, level) * 1000);
			e.newDamageModifierBuilder("Bleed")
					.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
					.setValue(getEarliest(DAMAGE, level) / 100D)
				.build();
		});
		return true;
	}
}