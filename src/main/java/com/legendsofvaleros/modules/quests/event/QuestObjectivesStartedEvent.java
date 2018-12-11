package com.legendsofvaleros.modules.quests.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import org.bukkit.event.HandlerList;

public class QuestObjectivesStartedEvent extends PlayerCharacterEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	private final IQuest quest;
	public IQuest getQuest() { return quest; }
	
	private final boolean first;
	public boolean isFirstGroup() { return first; }
	
	public QuestObjectivesStartedEvent(PlayerCharacter pc, IQuest quest, boolean first) {
		super(pc);
		
		this.quest = quest;
		this.first = first;
	}
}