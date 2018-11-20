package com.legendsofvaleros.modules.skills.quest;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import org.bukkit.event.Event;

public class SkillUseObjective extends AbstractObjective<ObjectiveProgressBoolean> {
	private String id;

	@Override
	public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
		return progress.value;
	}

	@Override
	public String getProgressText(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
		return "Use " + Skill.getSkillById(id).getUserFriendlyName(1);
	}

	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return "Used " + Skill.getSkillById(id).getUserFriendlyName(1);
	}

	@Override
	public Class<? extends Event>[] getRequestedEvents() {
		return new Class[] { SkillUsedEvent.class };
	}

	@Override
	public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressBoolean progress) {
		SkillUsedEvent e = (SkillUsedEvent)event;

		if(e.getSkill().getId().equals(id))
			progress.value = true;
	}
}