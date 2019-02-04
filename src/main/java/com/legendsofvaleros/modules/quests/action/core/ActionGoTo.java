package com.legendsofvaleros.modules.quests.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;

public class ActionGoTo extends AbstractQuestAction {
	int action;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		next.go(action);
	}
}