package com.legendsofvaleros.modules.skills.core.priest;

import com.legendsofvaleros.modules.skills.core.SkillTree;
import com.legendsofvaleros.modules.skills.core.mage.core.*;
import com.legendsofvaleros.modules.skills.core.mage.cryomancer.*;
import com.legendsofvaleros.modules.skills.core.mage.pyromancer.*;

public class TreePriest extends SkillTree {
	@Override
	public void initSkills() {
		
	}

	@Override
	public String getName() { return "The Priest"; }
	
	@Override
	public String[] getDescription() {
		return new String[] {
				"Adept Spellcaster. Using your knowledge",
				"of the arcane you hold the ability to",
				"instantly turn the tide of battle,",
				"whether it be for or against yourself."
			};
	}

	@Override
	public String[] getCoreSkills() {
		return new String[] {
				SkillFrostbolt.ID,
				SkillFireSurge.ID,
				SkillBlink.ID,
				SkillPolymorph.ID,
				SkillSpellCounter.ID,
				SkillTransformSelf.ID,
				SkillSpellTheft.ID
			};
	}

	@Override
	public SpecializedTree[] getSpecializedTrees() {
		return new SpecializedTree[] {
				new SpecializedTree("Pyromancer",
						SkillFireball.ID,
						SkillCriticalFlame.ID,
						SkillPyrostrike.ID,
						SkillFlamingStreak.ID,
						SkillFlameStrike.ID,
						SkillFlameShield.ID,
						SkillWalkOfFire.ID,
						SkillFirestorm.ID
					),
				new SpecializedTree("Cryomancer",
						SkillIcyBlast.ID,
						SkillIcicle.ID,
						SkillIceCrystal.ID,
						SkillAvalanche.ID,
						SkillCooling.ID,
						SkillFrostShield.ID,
						SkillSpikes.ID,
						SkillHailstorm.ID
					)
			};
	}
}