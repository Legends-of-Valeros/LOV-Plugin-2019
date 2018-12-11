package com.legendsofvaleros.modules.quests.action.stf;

import com.legendsofvaleros.modules.quests.Quests;

import java.util.HashMap;
import java.util.Set;

public class QuestActionFactory {
	private static final HashMap<String, Class<? extends AbstractQuestAction>> actionTypes = new HashMap<>();
	public static Set<String> getTypes() { return actionTypes.keySet(); }

	public static void registerType(String type, Class<? extends AbstractQuestAction> objClass) {
		Quests.getInstance().getLogger().finest("Registered action: " + type);
		actionTypes.put(type, objClass);
	}
	
	public static AbstractQuestAction newAction(String type) {
		try {
			return actionTypes.get(type).newInstance();
		} catch(Exception e) { }
		return null;
	}
}