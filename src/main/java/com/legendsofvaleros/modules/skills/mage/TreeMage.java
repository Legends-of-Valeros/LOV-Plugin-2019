package com.legendsofvaleros.modules.skills.mage;

import com.legendsofvaleros.modules.skills.SkillTree;
import com.legendsofvaleros.modules.skills.mage.core.*;
import com.legendsofvaleros.modules.skills.mage.cryomancer.*;
import com.legendsofvaleros.modules.skills.mage.pyromancer.*;

public class TreeMage extends SkillTree {
	@Override
	public void initSkills() {
		// Core
		new SkillFrostbolt();
		new SkillFireSurge();
		new SkillBlink();
		new SkillPolymorph();
		new SkillSpellCounter();
		new SkillTransformSelf();
		new SkillSpellTheft();
		
		// Pyromancer
		new SkillFireball();
		new SkillCriticalFlame();
		new SkillPyrostrike();
		new SkillFlamingStreak();
		new SkillFlameStrike();
		new SkillFlameShield();
		new SkillWalkOfFire();
		new SkillFirestorm();
		
		// Cryomancer
		new SkillIcyBlast();
		new SkillIcicle();
		new SkillIceCrystal();
		new SkillAvalanche();
		new SkillCooling();
		new SkillFrostShield();
		new SkillSpikes();
		new SkillHailstorm();
	}

	@Override
	public String getName() { return "The Mage"; }
	
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