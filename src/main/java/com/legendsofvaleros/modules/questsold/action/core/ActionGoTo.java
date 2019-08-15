package com.legendsofvaleros.modules.questsold.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public class ActionGoTo extends AbstractQuestAction {
	int action;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		next.go(action);
	}
}