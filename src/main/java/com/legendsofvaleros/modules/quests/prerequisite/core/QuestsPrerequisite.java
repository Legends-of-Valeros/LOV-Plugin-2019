package com.legendsofvaleros.modules.quests.prerequisite.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestPrerequisite;

public class QuestsPrerequisite implements IQuestPrerequisite {
	@Override public boolean canRepeat(IQuest quest, PlayerCharacter pc) { return true; }
	
	public String[] completed;

	@Override
	public boolean canAccept(IQuest quest, PlayerCharacter pc) {
		if(completed == null || completed.length == 0) return true;
		for(String quest_id : completed) {
			if(quest_id == null || quest_id.trim().length() == 0)
				continue;
			if(!QuestManager.completedQuests.contains(pc.getUniqueCharacterId(), quest_id))
				return false;
		}
		return true;
	}
}