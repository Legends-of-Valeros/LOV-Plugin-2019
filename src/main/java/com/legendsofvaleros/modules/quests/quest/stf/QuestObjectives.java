package com.legendsofvaleros.modules.quests.quest.stf;

import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;

public class QuestObjectives {
	public IObjective<?>[][] groups;
	public IObjective<?>[] getGroup(int i) {
		if(i == -1) return null;
		return groups[i];
	}
}