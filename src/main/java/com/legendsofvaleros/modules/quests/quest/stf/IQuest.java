package com.legendsofvaleros.modules.quests.quest.stf;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.stf.QuestActions;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.prerequisite.stf.IQuestPrerequisite;
import com.legendsofvaleros.modules.quests.progress.stf.QuestProgressPack;

import java.util.List;

public interface IQuest {
	/**
	 * @return The unique name used to identify this quest.
	 */
	String getId();
	
	/**
	 * @return Get the quest type.
	 */
	String getType();
	
	/**
	 * @return The list of prerequisite quests that must be completed before this one is available.
	 */
	List<IQuestPrerequisite> getPrerequisites();
	void addPrerequisite(IQuestPrerequisite prereq);
	
	/**
	 * @return The display name to show in UIs for the player.
	 */
	String getName();
	void setName(String name);

	/**
	 * @return A description to show for the quest in the quest book.
	 */
	String getDescription();
	void setDescription(String description);

	boolean isForced();
	void setForced(boolean forceAccept);
	
	boolean isRepeatable();
	void setRepeatable(boolean repeatable);
	
	QuestActions getActions();
	void setActions(QuestActions actions);
	void testResumeActions(PlayerCharacter pc);
	
	QuestObjectives getObjectives();
	void setObjectives(QuestObjectives objectives);
	
	int getCurrentGroupI(PlayerCharacter pc);
	IObjective<?>[] getCurrentGroup(PlayerCharacter pc);

	/**
	 * Called when a player talks to a quest NPC.
	 */
	void onTalk(PlayerCharacter pc, QuestStatus value);
	
	/**
	 * Called when the quest is declined.
	 */
	void onDecline(PlayerCharacter pc);
	
	/**
	 * Called when the quest is began.
	 */
	void onAccept(PlayerCharacter pc);
	
	/**
	 * Called when the quest is completed.
	 */
	void onCompleted(PlayerCharacter pc);

	boolean isCompleted(PlayerCharacter pc);
	void checkCompleted(PlayerCharacter pc);

	boolean hasProgress(PlayerCharacter pc);
	QuestProgressPack getProgress(PlayerCharacter pc);
	void loadProgress(PlayerCharacter pc, QuestProgressPack progress);
	void clearProgress(PlayerCharacter pc);
	
	void saveProgress(PlayerCharacter pc);
}