package com.legendsofvaleros.modules.quests.prerequisite.stf;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public interface IQuestPrerequisite {
	boolean canAccept(IQuest quest, PlayerCharacter pc);
	boolean canRepeat(IQuest quest, PlayerCharacter pc);
}