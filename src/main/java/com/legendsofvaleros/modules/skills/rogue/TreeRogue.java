package com.legendsofvaleros.modules.skills.rogue;

import com.legendsofvaleros.modules.skills.rogue.assassin.*;
import com.legendsofvaleros.modules.skills.rogue.core.*;
import com.legendsofvaleros.modules.skills.rogue.thief.*;
import com.legendsofvaleros.modules.skills.SkillTree;
import com.legendsofvaleros.modules.skills.rogue.assassin.SkillAmbush;
import com.legendsofvaleros.modules.skills.rogue.assassin.SkillBackstab;
import com.legendsofvaleros.modules.skills.rogue.assassin.SkillEviscerate;
import com.legendsofvaleros.modules.skills.rogue.assassin.SkillShuriken;
import com.legendsofvaleros.modules.skills.rogue.assassin.SkillSilenzio;
import com.legendsofvaleros.modules.skills.rogue.assassin.SkillSurprise;
import com.legendsofvaleros.modules.skills.rogue.assassin.SkillVenom;
import com.legendsofvaleros.modules.skills.rogue.assassin.SkillWound;
import com.legendsofvaleros.modules.skills.rogue.core.SkillCheapShot;
import com.legendsofvaleros.modules.skills.rogue.core.SkillCloak;
import com.legendsofvaleros.modules.skills.rogue.core.SkillPoison;
import com.legendsofvaleros.modules.skills.rogue.core.SkillRedFlask;
import com.legendsofvaleros.modules.skills.rogue.core.SkillRetreat;
import com.legendsofvaleros.modules.skills.rogue.core.SkillRoundhouseKick;
import com.legendsofvaleros.modules.skills.rogue.core.SkillSinisterStrike;
import com.legendsofvaleros.modules.skills.rogue.thief.SkillBlind;
import com.legendsofvaleros.modules.skills.rogue.thief.SkillDistract;
import com.legendsofvaleros.modules.skills.rogue.thief.SkillEvasiveMoves;
import com.legendsofvaleros.modules.skills.rogue.thief.SkillFrighten;
import com.legendsofvaleros.modules.skills.rogue.thief.SkillShadowStrike;
import com.legendsofvaleros.modules.skills.rogue.thief.SkillSmoke;
import com.legendsofvaleros.modules.skills.rogue.thief.SkillSprint;
import com.legendsofvaleros.modules.skills.rogue.thief.SkillStealth;
import com.legendsofvaleros.modules.skills.rogue.assassin.*;
import com.legendsofvaleros.modules.skills.rogue.core.*;
import com.legendsofvaleros.modules.skills.rogue.thief.*;

public class TreeRogue extends SkillTree {
	@Override
	public void initSkills() {
		// Core
		new SkillSinisterStrike();
		new SkillRedFlask();
		new SkillCloak();
		new SkillPoison();
		new SkillRoundhouseKick();
		new SkillRetreat();
		new SkillCheapShot();
		
		// Assassin
		new SkillBackstab();
		new SkillEviscerate();
		new SkillAmbush();
		new SkillSurprise();
		new SkillShuriken();
		new SkillVenom();
		new SkillWound();
		new SkillSilenzio();
		
		// Thief
		new SkillEvasiveMoves();
		new SkillSprint();
		new SkillStealth();
		new SkillSmoke();
		new SkillBlind();
		new SkillDistract();
		new SkillShadowStrike();
		new SkillFrighten();
	}
	
	@Override
	public String getName() { return "The Rogue"; }
	
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
				SkillSinisterStrike.ID,
				SkillRedFlask.ID,
				SkillCloak.ID,
				SkillPoison.ID,
				SkillRoundhouseKick.ID,
				SkillRetreat.ID,
				SkillCheapShot.ID
			};
	}

	@Override
	public SpecializedTree[] getSpecializedTrees() {
		return new SpecializedTree[] {
				new SpecializedTree("Assassin",
						SkillBackstab.ID,
						SkillEviscerate.ID,
						SkillAmbush.ID,
						SkillSurprise.ID,
						SkillShuriken.ID,
						SkillVenom.ID,
						SkillWound.ID,
						SkillSilenzio.ID
					),
				new SpecializedTree("Thief",
						SkillEvasiveMoves.ID,
						SkillSprint.ID,
						SkillStealth.ID,
						SkillSmoke.ID,
						SkillBlind.ID,
						SkillDistract.ID,
						SkillShadowStrike.ID,
						SkillFrighten.ID
					)
			};
	}
}