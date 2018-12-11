package com.legendsofvaleros.modules.quests.progress.stf;

public class QuestProgressPack {
	public final int group;
	public final ObjectiveProgressPack[] data;
	public Integer actionI;
	
	public QuestProgressPack(int group, int objectivesSize) {
		this.group = group;
		this.data = new ObjectiveProgressPack[objectivesSize];
	}

	public IQuestObjectiveProgress getForObjective(int objectiveI) {
		return data[objectiveI].progress;
	}
}