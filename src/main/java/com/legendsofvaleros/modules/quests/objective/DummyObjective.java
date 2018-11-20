package com.legendsofvaleros.modules.quests.objective;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import org.bukkit.event.Event;

public class DummyObjective extends AbstractObjective<ObjectiveProgressBoolean> {
	public String uncomplete;
	public String completed;
	public Integer objective;

	@Override
	public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
		if(objective == null) {
			for (IObjective<?> obj : getQuest().getCurrentGroup(pc))
				if (!(obj instanceof DummyObjective) && !obj.isCompleted(pc))
					return false;
		}else
			return getQuest().getCurrentGroup(pc)[objective].isCompleted(pc);
		return true;
	}

	@Override
	public String getProgressText(PlayerCharacter pc, ObjectiveProgressBoolean progress) { return uncomplete; }

	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return completed;
	}

	@Override
	public Class<? extends Event>[] getRequestedEvents() {
		return null;
	}

	@Override
	public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressBoolean progress) {

	}
}