package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

@FunctionalInterface
public interface IQuestPrerequisite {
    boolean canAccept(IQuest quest, PlayerCharacter pc);
}