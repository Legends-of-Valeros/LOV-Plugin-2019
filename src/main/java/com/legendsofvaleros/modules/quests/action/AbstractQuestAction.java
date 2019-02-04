package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.quests.api.IQuestAction;
import com.legendsofvaleros.modules.quests.api.IQuest;

import java.lang.ref.WeakReference;

public abstract class AbstractQuestAction implements IQuestAction {
	private WeakReference<IQuest> quest;
	@Override public IQuest getQuest() { return quest.get(); }

	private Integer groupI;
	@Override public Integer getGroupIndex() { return groupI; }

	private int actionI;
	@Override public int getActionIndex() { return actionI; }

	public EntityClass classLock;
	@Override public EntityClass getClassLock() { return classLock; }

	@Override
	public final void init(WeakReference<IQuest> quest, Integer groupI, int objectiveI) {
		this.quest = quest;
		this.groupI = groupI;
		this.actionI = objectiveI;

		onInit();
	}

	protected void onInit() { }
}