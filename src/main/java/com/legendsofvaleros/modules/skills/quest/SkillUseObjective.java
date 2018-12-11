package com.legendsofvaleros.modules.skills.quest;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import org.bukkit.event.Event;

public class SkillUseObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
	private String id;

	@Override
	public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
		return progress.value;
	}

	@Override
	public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
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
	public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
		SkillUsedEvent e = (SkillUsedEvent)event;

		if(e.getSkill().getId().equals(id))
			progress.value = true;
	}
}