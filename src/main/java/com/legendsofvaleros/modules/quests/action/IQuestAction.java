package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.quests.quest.IQuest;

import java.lang.ref.WeakReference;

public interface IQuestAction {
	void init(WeakReference<IQuest> quest, Integer groupI, int objectiveI);

	IQuest getQuest();
	Integer getGroupIndex();
	int getActionIndex();

	EntityClass getClassLock();

	void play(PlayerCharacter pc, Next next);

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