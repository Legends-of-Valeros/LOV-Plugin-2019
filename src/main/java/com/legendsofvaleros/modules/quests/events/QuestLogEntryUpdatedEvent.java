package com.legendsofvaleros.modules.quests.events;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.QuestLogEntry;
import org.bukkit.event.HandlerList;

public class QuestLogEntryUpdatedEvent extends QuestLogEntryEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	private boolean wasSuccess;

	public boolean wasSuccess() { return wasSuccess; }

	public QuestLogEntryUpdatedEvent(IQuestInstance instance, QuestLogEntry entry, boolean wasSuccess) {
		super(instance, entry);

		this.wasSuccess = wasSuccess;
	}
}