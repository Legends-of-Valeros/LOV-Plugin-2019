package com.legendsofvaleros.modules.skills.warrior;

import com.legendsofvaleros.modules.skills.warrior.berserker.*;
import com.legendsofvaleros.modules.skills.warrior.core.*;
import com.legendsofvaleros.modules.skills.warrior.guardian.*;
import com.legendsofvaleros.modules.skills.SkillTree;
import com.legendsofvaleros.modules.skills.warrior.berserker.SkillDeadwish;
import com.legendsofvaleros.modules.skills.warrior.berserker.SkillDisarm;
import com.legendsofvaleros.modules.skills.warrior.berserker.SkillEnrage;
import com.legendsofvaleros.modules.skills.warrior.berserker.SkillIronHide;
import com.legendsofvaleros.modules.skills.warrior.berserker.SkillRush;
import com.legendsofvaleros.modules.skills.warrior.berserker.SkillStunningStomp;
import com.legendsofvaleros.modules.skills.warrior.berserker.SkillTrueStrike;
import com.legendsofvaleros.modules.skills.warrior.berserker.SkillWhirlwind;
import com.legendsofvaleros.modules.skills.warrior.core.SkillDestructiveLeap;
import com.legendsofvaleros.modules.skills.warrior.core.SkillExecution;
import com.legendsofvaleros.modules.skills.warrior.core.SkillPummel;
import com.legendsofvaleros.modules.skills.warrior.core.SkillRefresh;
import com.legendsofvaleros.modules.skills.warrior.core.SkillSlash;
import com.legendsofvaleros.modules.skills.warrior.core.SkillTaunt;
import com.legendsofvaleros.modules.skills.warrior.core.SkillWarCry;
import com.legendsofvaleros.modules.skills.warrior.guardian.SkillAbsorb;
import com.legendsofvaleros.modules.skills.warrior.guardian.SkillBleed;
import com.legendsofvaleros.modules.skills.warrior.guardian.SkillExecute;
import com.legendsofvaleros.modules.skills.warrior.guardian.SkillFortress;
import com.legendsofvaleros.modules.skills.warrior.guardian.SkillMagicArmor;
import com.legendsofvaleros.modules.skills.warrior.guardian.SkillShieldSlam;
import com.legendsofvaleros.modules.skills.warrior.guardian.SkillSkullCrack;
import com.legendsofvaleros.modules.skills.warrior.guardian.SkillSunder;
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