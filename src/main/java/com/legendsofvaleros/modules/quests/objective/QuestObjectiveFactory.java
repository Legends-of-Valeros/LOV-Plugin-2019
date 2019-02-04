package com.legendsofvaleros.modules.quests.objective;

import com.legendsofvaleros.modules.quests.QuestController;

import java.util.HashMap;
import java.util.Set;

public class QuestObjectiveFactory {
	private static HashMap<String, Class<? extends IQuestObjective<?>>> objectiveTypes = new HashMap<>();
	public static Set<String> getTypes() { return objectiveTypes.keySet(); }

	public static void registerType(String type, Class<? extends IQuestObjective<?>> objClass) {
		QuestController.getInstance().getLogger().finest("Registered objective: " + type);
		objectiveTypes.put(type, objClass);
	}
	
	public static IQuestObjective<?> newObjective(String type) {
		try {
			return objectiveTypes.get(type).newInstance();
		} catch(Exception e) { }
		return null;
	}
}