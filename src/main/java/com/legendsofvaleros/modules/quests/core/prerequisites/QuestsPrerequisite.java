package com.legendsofvaleros.modules.quests.core.prerequisites;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.api.IQuest;

public class QuestsPrerequisite implements IQuestPrerequisite {
	public String[] completed;

	@Override
	public boolean canAccept(IQuest quest, PlayerCharacter pc) {
		if(completed == null || completed.length == 0) return true;
		for(String quest_id : completed) {
			if(quest_id == null || quest_id.trim().length() == 0)
				continue;
			//if(!QuestController.getInstance().completedQuests.contains(pc.getUniqueCharacterId(), quest_id))
			//	return false;
		}
		return true;
	}
}