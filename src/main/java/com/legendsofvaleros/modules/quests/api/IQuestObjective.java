package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.lang.ref.WeakReference;

/**
 * Warning! Objectives are registered as listeners <i>once.</i> Not for each objective instance.
 * @author Stumblinbear
 */
public interface IQuestObjective<T extends IQuestObjectiveProgress> extends Listener, IQuestEventReceiver {
	void init(WeakReference<IQuest> quest, int groupI, int objectiveI);

	IQuest getQuest();
	int getGroupIndex();
	int getObjectiveIndex();

	T getProgress(PlayerCharacter pc);

	/**
	 * @return If the objective should be shown in the quest log.
	 */
	boolean isVisible();

	String getProgressText(PlayerCharacter pc);

	/**
	 * @return The text to display when showing the completed objective.
	 */
	String getCompletedText(PlayerCharacter pc);

	/**
	 * @return Where the player's compass should point.
	 */
	Location getLocation(PlayerCharacter pc);

	void onBegin(PlayerCharacter pc);

	boolean isCompleted(PlayerCharacter pc);

	void onEnd(PlayerCharacter pc);

	/**
	 * How often the update function should be fired in ticks per second.
	 * Minimum resolution is 1, meaning 20 times per second.
	 */
	int getUpdateTimer();

	/**
	 * Fires each tick while the player has the quest accepted.
	 * @param ticks is the number of ticks since the objective was started.
	 */
	void onUpdate(PlayerCharacter pc, int ticks);
}