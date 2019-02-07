package com.legendsofvaleros.modules.quests.objective.skills;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class SkillUseObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
	private String id;

	private transient Skill skill;

	@Override
	protected void onInit() {
		if ((skill = Skill.getSkillById(id)) == null) {
			MessageUtil.sendException(QuestController.getInstance(), "No skill with that ID in gear. Offender: " + id + " in " + getQuest().getId());
			return;
		}
	}

	@Override
	public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
		return progress.value;
	}

	@Override
	public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
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
	public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
		SkillUsedEvent e = (SkillUsedEvent)event;

		if(e.getSkill().getId().equals(id))
			progress.value = true;
	}
}