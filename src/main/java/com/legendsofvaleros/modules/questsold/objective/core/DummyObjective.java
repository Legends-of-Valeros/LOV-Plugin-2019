package com.legendsofvaleros.modules.questsold.objective.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.questsold.api.IQuestObjective;
import org.bukkit.event.Event;

public class DummyObjective extends AbstractQuestObjective<Boolean> {
	public String uncomplete;
	public String completed;
	public Integer objective;

	@Override
	public boolean isCompleted(PlayerCharacter pc, Boolean progress) {
		if(objective == null) {
			for (IQuestObjective<?> obj : getQuest().getObjectiveGroup(pc))
				if (!(obj instanceof DummyObjective) && !obj.isCompleted(pc))
					return false;
		}else
			return getQuest().getObjectiveGroup(pc)[objective].isCompleted(pc);
		return true;
	}

	@Override
	public String getProgressText(PlayerCharacter pc, Boolean progress) { return uncomplete; }

	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return completed;
	}

	@Override
	public Class<? extends Event>[] getRequestedEvents() {
		return null;
	}

	@Override
	public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
		return progress;
	}
}