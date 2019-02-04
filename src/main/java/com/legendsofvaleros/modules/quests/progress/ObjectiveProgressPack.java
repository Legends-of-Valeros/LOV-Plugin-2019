package com.legendsofvaleros.modules.quests.progress;

import com.legendsofvaleros.modules.quests.api.IQuestObjectiveProgress;

public class ObjectiveProgressPack {
	public final String type;
	public final IQuestObjectiveProgress progress;

	public ObjectiveProgressPack(IQuestObjectiveProgress data) {
		this.type = ProgressFactory.getType(data);
		this.progress = data;
	}
}