package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.questsold.action.QuestActions;
import com.legendsofvaleros.modules.questsold.api.IQuestAction;
import com.legendsofvaleros.modules.questsold.api.IQuestObjective;
import com.legendsofvaleros.modules.questsold.api.IQuestPrerequisite;
import com.legendsofvaleros.modules.questsold.core.QuestObjectives;
import com.legendsofvaleros.modules.questsold.core.QuestProgressPack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IQuest {
	/**
	 * @return The unique name used to identify this gear.
	 */
	String getId();

	/**
	 * @return The display name to show in UIs for the player.
	 */
	String getName();

	/**
	 * @return A description to show for the quest in the quest book.
	 */
	String getDescription();

	boolean isForced();

	/**
	 * @return The list of prerequisite quests that must be completed before this one is available.
	 */
	List<IQuestPrerequisite> getPrerequisites();

	/**
	 * @return The options that define how a quest can be repeated.
	 */
	void getRepeatOptions();

	/**
	 * @return A map of Nodes and their respective UUIDs.
	 */
	Map<UUID, INode> getNodes();


	QuestActions getActions();
	Integer getActionGroupI(PlayerCharacter pc);
	IQuestAction[] getActionGroup(PlayerCharacter pc);

	void testResumeActions(PlayerCharacter pc);

	QuestObjectives getObjectives();
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
}