package com.legendsofvaleros.modules.quests.objective.skills;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.skills.event.BindSkillEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class SkillBindObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
	private String id;
	private int slot;

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
		return "Bind " + (skill == null ? "UNKNOWN" : skill.getUserFriendlyName(1)) + (slot >= 0 ? " to slot " + (slot + 1) : "");
	}
	
	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return "Bound " + (skill == null ? "UNKNOWN" : skill.getUserFriendlyName(1)) + (slot >= 0 ? " to slot " + (slot + 1) : "");
	}

	@Override
	public Class<? extends Event>[] getRequestedEvents() {
		return new Class[] { BindSkillEvent.class };
	}

	@Override
	public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
		BindSkillEvent e = (BindSkillEvent)event;

		if((slot == -1 || e.getSlot() == slot) && e.getSkillId().equals(id))
			progress.value = true;
	}
}