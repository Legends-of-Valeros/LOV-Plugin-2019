package com.legendsofvaleros.modules.quests.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.IQuest;
import org.bukkit.event.HandlerList;

public class QuestStartedEvent extends QuestEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	public QuestStartedEvent(PlayerCharacter pc, IQuest quest) {
		super(pc, quest);
	}
}