package com.legendsofvaleros.modules.quests.objective;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.objective.stf.IQuestObjective;
import com.legendsofvaleros.modules.quests.progress.QuestObjectiveProgressBoolean;
import org.bukkit.event.Event;

public class DummyObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
	public String uncomplete;
	public String completed;
	public Integer objective;

	@Override
	public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
		if(objective == null) {
			for (IQuestObjective<?> obj : getQuest().getObjectiveGroup(pc))
				if (!(obj instanceof DummyObjective) && !obj.isCompleted(pc))
					return false;
		}else
			return getQuest().getObjectiveGroup(pc)[objective].isCompleted(pc);
		return true;
	}

	@Override
	public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) { return uncomplete; }

	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return completed;
	}

	@Override
	public Class<? extends Event>[] getRequestedEvents() {
		return null;
	}

	@Override
	public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {

	}
}