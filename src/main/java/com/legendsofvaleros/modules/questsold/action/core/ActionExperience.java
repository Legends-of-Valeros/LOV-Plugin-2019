package com.legendsofvaleros.modules.questsold.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public class ActionExperience extends AbstractQuestAction {
	long amount;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		pc.getExperience().addExperience(amount, false);
		
		next.go();
	}
}