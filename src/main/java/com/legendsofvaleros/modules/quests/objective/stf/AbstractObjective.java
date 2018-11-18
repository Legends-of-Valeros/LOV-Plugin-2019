package com.legendsofvaleros.modules.quests.objective.stf;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.progress.stf.IObjectiveProgress;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.Location;
import org.bukkit.event.Event;

import java.lang.ref.WeakReference;

public abstract class AbstractObjective<T extends IObjectiveProgress> implements IObjective<T> {
	private WeakReference<IQuest> quest;
	public IQuest getQuest() { return quest.get(); }

	private int groupI;
	public int getGroupIndex() { return groupI; }

	private int objectiveI;
	public int getObjectiveIndex() { return objectiveI; }

	private boolean visible = true;
	
	@Override
	public final void init(WeakReference<IQuest> quest, int groupI, int objectiveI) {
		this.quest = quest;
		this.groupI = groupI;
		this.objectiveI = objectiveI;

		onInit();
	}

	protected void onInit() { }

	@Override
	public Location getLocation(PlayerCharacter pc) {
		return null;
	}

	@Override
	public final T getProgress(PlayerCharacter pc) {
		return (T)getQuest().getProgress(pc).getForObjective(getObjectiveIndex());
	}

	@Override
	public final boolean isVisible() { return visible; }

	@Override
	public final String getProgressText(PlayerCharacter pc) { return getProgressText(pc, getProgress(pc)); }
	public abstract String getProgressText(PlayerCharacter pc, T progress);

	@Override
	public final boolean isCompleted(PlayerCharacter pc) {
		return isCompleted(pc, getProgress(pc));
	}
	public abstract boolean isCompleted(PlayerCharacter pc, T progress);

	@Override
	public final void onBegin(PlayerCharacter pc) { onBegin(pc, getProgress(pc)); }
	public void onBegin(PlayerCharacter pc, T progress) { }

	@Override
	public final void onEnd(PlayerCharacter pc) { onEnd(pc, getProgress(pc)); }
	public void onEnd(PlayerCharacter pc, T progress) { }

	@Override
	public final void onEvent(Event event, PlayerCharacter pc) { onEvent(event, pc, getProgress(pc)); }
	public abstract void onEvent(Event event, PlayerCharacter pc, T progress);
}