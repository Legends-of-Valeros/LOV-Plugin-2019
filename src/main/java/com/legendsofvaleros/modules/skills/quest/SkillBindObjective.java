package com.legendsofvaleros.modules.skills.quest;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.skills.event.BindSkillEvent;
import org.bukkit.event.Event;

public class SkillBindObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
	private String id;
	private int slot;

	@Override
	public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
		return progress.value;
	}
	
	@Override
	public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
		return "Bind " + Skill.getSkillById(id).getUserFriendlyName(1) + (slot >= 0 ? " to slot " + (slot + 1) : "");
	}
	
	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return "Bound " + Skill.getSkillById(id).getUserFriendlyName(1) + (slot >= 0 ? " to slot " + (slot + 1) : "");
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