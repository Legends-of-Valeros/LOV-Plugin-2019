package com.legendsofvaleros.modules.quests.progress;

import com.legendsofvaleros.modules.quests.api.IQuestObjectiveProgress;

public class QuestProgressPack {
	public final Integer group;
	public final ObjectiveProgressPack[] data;
	public Integer actionI;
	
	public QuestProgressPack(Integer group, int objectivesSize) {
		this.group = group;
		this.data = new ObjectiveProgressPack[objectivesSize];
	}

	public IQuestObjectiveProgress getForObjective(int objectiveI) {
		return data[objectiveI].progress;
	}
}