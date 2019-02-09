package com.legendsofvaleros.modules.quests.objective.skills;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class SkillUseObjective extends AbstractQuestObjective<Boolean> {
	private String id;

	private transient Skill skill;

	@Override
	protected void onInit() {
		if ((skill = Skill.getSkillById(id)) == null) {
			MessageUtil.sendException(QuestController.getInstance(), "No skill with that ID in quest. Offender: " + id + " in " + getQuest().getId());
			return;
		}
	}

	@Override
	public Boolean onBegin(PlayerCharacter pc, Boolean progress) {
		return false;
	}

	@Override
	public boolean isCompleted(PlayerCharacter pc, Boolean progress) {
		return progress;
	}

	@Override
	public String getProgressText(PlayerCharacter pc, Boolean progress) {
		return "Use " + (skill == null ? "UNKNOWN" : skill.getUserFriendlyName(1));
	}

	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return "Used " + (skill == null ? "UNKNOWN" : skill.getUserFriendlyName(1));
	}

	@Override
	public Class<? extends Event>[] getRequestedEvents() {
		return new Class[] { SkillUsedEvent.class };
	}

	@Override
	public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
		SkillUsedEvent e = (SkillUsedEvent)event;

		if(e.getSkill().getId().equals(id))
			return true;
		return false;
	}
}