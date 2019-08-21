package com.legendsofvaleros.modules.quests.events;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import org.bukkit.event.HandlerList;

public class QuestStartedEvent extends QuestInstanceEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	public QuestStartedEvent(IQuestInstance instance) {
		super(instance);
	}
}