package com.legendsofvaleros.modules.quests.events;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.QuestLogEntry;
import org.bukkit.event.HandlerList;

public class QuestLogEntryRemovedEvent extends QuestLogEntryEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	public QuestLogEntryRemovedEvent(IQuestInstance instance, QuestLogEntry entry) {
		super(instance, entry);
	}
}