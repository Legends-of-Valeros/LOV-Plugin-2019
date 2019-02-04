package com.legendsofvaleros.modules.quests.prerequisite.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.api.IQuest;

public class LevelPrerequisite implements IQuestPrerequisite {
	@Override public boolean canRepeat(IQuest quest, PlayerCharacter pc) { return true; }
	
	public int min = 0;

	@Override
	public boolean canAccept(IQuest quest, PlayerCharacter pc) {
		return pc.getExperience().getLevel() >= min;
	}
}