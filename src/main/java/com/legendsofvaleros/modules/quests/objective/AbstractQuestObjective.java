package com.legendsofvaleros.modules.quests.objective;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestObjective;
import org.bukkit.Location;
import org.bukkit.event.Event;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;

public abstract class AbstractQuestObjective<T> implements IQuestObjective<T> {
	private WeakReference<IQuest> quest;
	@Override public IQuest getQuest() { return quest.get(); }

	private int groupI;
	@Override public int getGroupIndex() { return groupI; }

	private int objectiveI;
	@Override public int getObjectiveIndex() { return objectiveI; }

	@Override
	public Class<T> getProgressClass() {
        return (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

	private boolean visible = true;

	@SerializedName("template_active")
	private String templateActive;

	@SerializedName("template_completed")
	private String templateCompleted;

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

	private final void setProgress(PlayerCharacter pc, T obj) {
        getQuest().getProgress(pc).setForObjective(getObjectiveIndex(), obj);
    }

	@Override
	public final boolean isVisible() { return visible; }

	@Override
	public final String getProgressText(PlayerCharacter pc) {
		// TODO: Use a map to let objectives add replacement variables to these templates
		// this will let us override any objective text on the panel without using a dummy
		// objective
		if(getProgress(pc) != null) {
			if(templateActive != null) {
				return templateActive;
			}
		}else{
			if(templateCompleted != null) {
				return templateCompleted;
			}
		}

		return getProgressText(pc, getProgress(pc));
	}
	public abstract String getProgressText(PlayerCharacter pc, T progress);

	@Override
	public final boolean isCompleted(PlayerCharacter pc) {
		return isCompleted(pc, getProgress(pc));
	}
	public abstract boolean isCompleted(PlayerCharacter pc, T progress);

	@Override
	public final void onBegin(PlayerCharacter pc) { setProgress(pc, onStart(pc)); }
	public T onStart(PlayerCharacter pc) { return null; }

	@Override
	public final void onEnd(PlayerCharacter pc) { setProgress(pc, onEnd(pc, getProgress(pc))); }
	public T onEnd(PlayerCharacter pc, T progress) { return progress; }

	@Override
	public final void onEvent(Event event, PlayerCharacter pc) { setProgress(pc, onEvent(event, pc, getProgress(pc))); }
	public abstract T onEvent(Event event, PlayerCharacter pc, T progress);

	@Override
	public int getUpdateTimer() { return 0; }
	@Override
	public final void onUpdate(PlayerCharacter pc, int ticks) { setProgress(pc, onUpdate(pc, getProgress(pc), ticks)); }
	public T onUpdate(PlayerCharacter pc, T progress, int ticks) { return progress; }
}