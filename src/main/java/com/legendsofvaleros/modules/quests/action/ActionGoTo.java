package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import org.bukkit.entity.Player;

public class ActionGoTo extends AbstractQuestAction {
	int action;
	
	@Override
	public void play(Player player, Next next) {
		next.go(action);
	}
}