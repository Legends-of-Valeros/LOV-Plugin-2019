package com.legendsofvaleros.modules.quests.objective.stf;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.progress.stf.IObjectiveProgress;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.lang.ref.WeakReference;

/**
 * Warning! Objectives are registered as listeners <i>once.</i> Not for each objective instance.
 * @author Stumblinbear
 */
public interface IObjective<T extends IObjectiveProgress> extends Listener {
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
	 * @return The bukkit events to receive in onEvent
	 */
	Class<? extends Event>[] getRequestedEvents();

	void onEvent(Event event, PlayerCharacter pc);
}