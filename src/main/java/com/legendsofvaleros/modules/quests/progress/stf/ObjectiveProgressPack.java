package com.legendsofvaleros.modules.quests.progress.stf;

public class ObjectiveProgressPack {
	public final String type;
	public final IQuestObjectiveProgress progress;

	public ObjectiveProgressPack(IQuestObjectiveProgress data) {
		this.type = ProgressFactory.getType(data);
		this.progress = data;
	}
}