package com.legendsofvaleros.modules.quests.prerequisite.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.prerequisite.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.core.IQuest;

public class RacePrerequisite implements IQuestPrerequisite {
	@Override public boolean canRepeat(IQuest quest, PlayerCharacter pc) { return true; }
	
	public String is = "";

	@Override
	public boolean canAccept(IQuest quest, PlayerCharacter pc) {
		return pc.getPlayerRace().name().equals(is);
	}
}