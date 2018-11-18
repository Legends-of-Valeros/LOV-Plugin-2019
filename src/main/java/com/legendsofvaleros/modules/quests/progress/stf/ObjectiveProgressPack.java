package com.legendsofvaleros.modules.quests.progress.stf;

public class ObjectiveProgressPack {
	public final String type;
	public final IObjectiveProgress progress;

	public ObjectiveProgressPack(IObjectiveProgress data) {
		this.type = ProgressFactory.getType(data);
		this.progress = data;
	}
}