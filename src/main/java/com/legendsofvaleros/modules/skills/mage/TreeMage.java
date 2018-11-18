package com.legendsofvaleros.modules.skills.mage;

import com.legendsofvaleros.modules.skills.mage.core.*;
import com.legendsofvaleros.modules.skills.mage.cryomancer.*;
import com.legendsofvaleros.modules.skills.mage.pyromancer.*;
import com.legendsofvaleros.modules.skills.SkillTree;
import com.legendsofvaleros.modules.skills.mage.core.SkillBlink;
import com.legendsofvaleros.modules.skills.mage.core.SkillFireSurge;
import com.legendsofvaleros.modules.skills.mage.core.SkillFrostbolt;
import com.legendsofvaleros.modules.skills.mage.core.SkillPolymorph;
import com.legendsofvaleros.modules.skills.mage.core.SkillSpellCounter;
import com.legendsofvaleros.modules.skills.mage.core.SkillSpellTheft;
import com.legendsofvaleros.modules.skills.mage.core.SkillTransformSelf;
import com.legendsofvaleros.modules.skills.mage.cryomancer.SkillAvalanche;
import com.legendsofvaleros.modules.skills.mage.cryomancer.SkillCooling;
import com.legendsofvaleros.modules.skills.mage.cryomancer.SkillFrostShield;
import com.legendsofvaleros.modules.skills.mage.cryomancer.SkillHailstorm;
import com.legendsofvaleros.modules.skills.mage.cryomancer.SkillIceCrystal;
import com.legendsofvaleros.modules.skills.mage.cryomancer.SkillIcicle;
import com.legendsofvaleros.modules.skills.mage.cryomancer.SkillIcyBlast;
import com.legendsofvaleros.modules.skills.mage.cryomancer.SkillSpikes;
import com.legendsofvaleros.modules.skills.mage.pyromancer.SkillCriticalFlame;
import com.legendsofvaleros.modules.skills.mage.pyromancer.SkillFireball;
import com.legendsofvaleros.modules.skills.mage.pyromancer.SkillFirestorm;
import com.legendsofvaleros.modules.skills.mage.pyromancer.SkillFlameShield;
import com.legendsofvaleros.modules.skills.mage.pyromancer.SkillFlameStrike;
import com.legendsofvaleros.modules.skills.mage.pyromancer.SkillFlamingStreak;
import com.legendsofvaleros.modules.skills.mage.pyromancer.SkillPyrostrike;
import com.legendsofvaleros.modules.skills.mage.pyromancer.SkillWalkOfFire;
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