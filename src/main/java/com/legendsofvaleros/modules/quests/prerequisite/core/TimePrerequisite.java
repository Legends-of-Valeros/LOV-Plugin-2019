package com.legendsofvaleros.modules.quests.prerequisite.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestPrerequisite;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimePrerequisite implements IQuestPrerequisite {
	public LocalDateTime[] between;

	public Duration since;
	
	@Override
	public boolean canAccept(IQuest quest, PlayerCharacter pc) {
		return (LocalDateTime.now().isAfter(between[0]) && LocalDateTime.now().isBefore(between[1]));
	}

	@Override
	public boolean canRepeat(IQuest quest, PlayerCharacter pc) {
		return LocalDateTime.now().isBefore(QuestController.getInstance().completedQuests.get(pc.getUniqueCharacterId(), quest.getId()).plus(since));
	}
}