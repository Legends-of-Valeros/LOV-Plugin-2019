package com.legendsofvaleros.modules.quests.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import org.bukkit.event.HandlerList;

public abstract class QuestInstanceEvent extends PlayerCharacterEvent {
	private final IQuestInstance instance;

	public IQuestInstance getInstance() { return instance; }

	public IQuest getQuest() { return instance.getQuest(); }

	public QuestInstanceEvent(IQuestInstance instance) {
		super(instance.getPlayerCharacter());

		this.instance = instance;
	}
}