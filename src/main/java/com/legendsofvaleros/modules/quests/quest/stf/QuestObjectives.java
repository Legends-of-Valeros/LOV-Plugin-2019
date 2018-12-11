package com.legendsofvaleros.modules.quests.quest.stf;

import com.legendsofvaleros.modules.quests.objective.stf.IQuestObjective;

public class QuestObjectives {
	public IQuestObjective<?>[][] groups;
	public IQuestObjective<?>[] getGroup(int i) {
		if(i == -1) return null;
		return groups[i];
	}
}