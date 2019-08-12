package com.legendsofvaleros.modules.questsold.prerequisite.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.questsold.api.IQuest;
import com.legendsofvaleros.modules.questsold.api.IQuestPrerequisite;

public class ClassPrerequisite implements IQuestPrerequisite {
	@Override public boolean canRepeat(IQuest quest, PlayerCharacter pc) { return true; }
	
	public String is = "";

	@Override
	public boolean canAccept(IQuest quest, PlayerCharacter pc) {
		return pc.getPlayerClass().name().equals(is);
	}
}