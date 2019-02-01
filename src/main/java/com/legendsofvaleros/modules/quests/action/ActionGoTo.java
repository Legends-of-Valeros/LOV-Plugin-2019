package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;

public class ActionGoTo extends AbstractQuestAction {
	int action;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		next.go(action);
	}
}