package com.legendsofvaleros.modules.quests.objective.stf;

import java.util.HashMap;
import java.util.Set;

public class ObjectiveFactory {
	private static HashMap<String, Class<? extends IObjective<?>>> objectiveTypes = new HashMap<>();
	public static Set<String> getTypes() { return objectiveTypes.keySet(); }

	public static void registerType(String type, Class<? extends IObjective<?>> objClass) {
		objectiveTypes.put(type, objClass);
	}
	
	public static IObjective<?> newObjective(String type) {
		try {
			return objectiveTypes.get(type).newInstance();
		} catch(Exception e) { }
		return null;
	}
}