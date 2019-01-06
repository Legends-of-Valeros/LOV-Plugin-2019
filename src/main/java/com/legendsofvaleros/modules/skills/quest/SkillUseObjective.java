package com.legendsofvaleros.modules.skills.quest;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.skills.event.SkillUsedEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class SkillUseObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
	private String id;

	private transient Skill skill;

	@Override
	protected void onInit() {
		if ((skill = Skill.getSkillById(id)) == null) {
			MessageUtil.sendException(Quests.getInstance(), "No skill with that ID in quest. Offender: " + id + " in " + getQuest().getId(), false);
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