package com.legendsofvaleros.modules.questsold.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public interface IQuestPrerequisite {
	boolean canAccept(IQuest quest, PlayerCharacter pc);
	boolean canRepeat(IQuest quest, PlayerCharacter pc);
}