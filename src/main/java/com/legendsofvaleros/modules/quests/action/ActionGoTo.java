package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import org.bukkit.entity.Player;

import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;

public class ActionGoTo extends AbstractAction {
	int action;
	
	@Override
	public void play(Player player, Next next) {
		next.go(action);
	}
}