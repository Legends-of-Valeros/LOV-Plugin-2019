package com.legendsofvaleros.modules.skills.warrior;

import com.legendsofvaleros.modules.skills.core.SkillTree;
import com.legendsofvaleros.modules.skills.warrior.berserker.*;
import com.legendsofvaleros.modules.skills.warrior.core.*;
import com.legendsofvaleros.modules.skills.warrior.guardian.*;

public class TreeWarrior extends SkillTree {
	@Override
	public void initSkills() {
		// Core
		new SkillSlash();
		new SkillTaunt();
		new SkillExecution();
		new SkillRefresh();
		new SkillPummel();
		new SkillDestructiveLeap();
		new SkillWarCry();
		
		// Berserker
		new SkillRush();
		new SkillDisarm();
		new SkillDeadwish();
		new SkillTrueStrike();
		new SkillStunningStomp();
		new SkillEnrage();
		new SkillIronHide();
		new SkillWhirlwind();

		// Guardian
		new SkillFortress();
		new SkillShieldSlam();
		new SkillExecute();
		new SkillSkullCrack();
		new SkillMagicArmor();
		new SkillSunder();
		new SkillBleed();
		new SkillAbsorb();
	}
	
	@Override
	public String getName() { return "The Warrior"; }
	
	@Override
	public String[] getDescription() {
		return new String[] {
				"Combat Exemplified. With your strength and",
				"fearlessness, you can bring the skill and",
				"determination that is needed fight back the",
				"enemy and bring glory to the motherland."
			};
	}

	@Override
	public String[] getCoreSkills() {
		return new String[] {
				SkillSlash.ID,
				SkillTaunt.ID,
				SkillExecution.ID,
				SkillRefresh.ID,
				SkillPummel.ID,
				SkillDestructiveLeap.ID,
				SkillWarCry.ID
			};
	}

	@Override
	public SpecializedTree[] getSpecializedTrees() {
		return new SpecializedTree[] {
				new SpecializedTree("Berserker",
						SkillRush.ID,
						SkillDisarm.ID,
						SkillDeadwish.ID,
						SkillTrueStrike.ID,
						SkillStunningStomp.ID,
						SkillEnrage.ID,
						SkillIronHide.ID,
						SkillWhirlwind.ID
					),
				new SpecializedTree("Guardian",
						SkillFortress.ID,
						SkillShieldSlam.ID,
						SkillExecute.ID,
						SkillSkullCrack.ID,
						SkillMagicArmor.ID,
						SkillSunder.ID,
						SkillBleed.ID,
						SkillAbsorb.ID
					)
			};
	}
}