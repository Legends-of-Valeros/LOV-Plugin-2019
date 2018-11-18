package com.legendsofvaleros.modules.quests.action.stf;

import org.bukkit.entity.Player;

public interface IQuestAction {
	void play(Player player, Next next);

	abstract class Next {
		public void go() {
			run(null);
		}
		
		public void go(int actionI) {
			run(actionI);
		}

		public abstract void run(Integer action);
	}
}