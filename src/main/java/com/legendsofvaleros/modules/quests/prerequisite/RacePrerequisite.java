package com.legendsofvaleros.modules.quests.prerequisite;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.prerequisite.stf.IQuestPrerequisite;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.prerequisite.stf.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.prerequisite.stf.IQuestPrerequisite;

public class RacePrerequisite implements IQuestPrerequisite {
	@Override public boolean canRepeat(IQuest quest, PlayerCharacter pc) { return true; }
	
	public String is = "";

	@Override
	public boolean canAccept(IQuest quest, PlayerCharacter pc) {
		return pc.getPlayerRace().name().equals(is);
	}
}