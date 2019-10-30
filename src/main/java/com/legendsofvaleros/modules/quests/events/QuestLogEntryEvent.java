package com.legendsofvaleros.modules.quests.events;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.QuestLogEntry;

public abstract class QuestLogEntryEvent extends QuestInstanceEvent {
	private QuestLogEntry entry;

	public QuestLogEntry getEntry() { return entry; }

	public QuestLogEntryEvent(IQuestInstance instance, QuestLogEntry entry) {
		super(instance);

		this.entry = entry;
	}
}