package com.legendsofvaleros.modules.quests.progress;

public class ObjectiveProgressPack {
	public final String type;
	public final IQuestObjectiveProgress progress;

	public ObjectiveProgressPack(IQuestObjectiveProgress data) {
		this.type = ProgressFactory.getType(data);
		this.progress = data;
	}
}