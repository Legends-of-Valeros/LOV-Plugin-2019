package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.QuestActions;
import com.legendsofvaleros.modules.quests.core.QuestObjectives;
import com.legendsofvaleros.modules.quests.core.QuestProgressPack;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IQuest {
	/**
	 * @return The unique name used to identify this gear.
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

	Integer getActionGroupI(PlayerCharacter pc);
	IQuestAction[] getActionGroup(PlayerCharacter pc);


	QuestObjectives getObjectives();
	void setObjectives(QuestObjectives objectives);
	
	Integer getObjectiveGroupI(PlayerCharacter pc);
	IQuestObjective<?>[] getObjectiveGroup(PlayerCharacter pc);


	/**
	 * Called when a player starts a gear.
	 */
	void onStart(PlayerCharacter pc);
	
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

	Set<Map.Entry<CharacterId, QuestProgressPack>> getProgressions();

	boolean hasProgress(PlayerCharacter pc);
	QuestProgressPack getProgress(PlayerCharacter pc);
	void loadProgress(PlayerCharacter pc, QuestProgressPack progress);
	void clearProgress(PlayerCharacter pc);
	
	void saveProgress(PlayerCharacter pc);
}