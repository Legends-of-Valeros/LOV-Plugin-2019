package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuestAction;

import java.util.HashMap;
import java.util.Set;

public class QuestActionFactory {
	private static final HashMap<String, Class<? extends AbstractQuestAction>> actionTypes = new HashMap<>();
	public static Set<String> getTypes() { return actionTypes.keySet(); }
	public static Class<? extends AbstractQuestAction> getType(String id) { return actionTypes.get(id); }
	public static void registerType(String type, Class<? extends AbstractQuestAction> objClass) {
		QuestController.getInstance().getLogger().finest("Registered action: " + type);
		actionTypes.put(type, objClass);
	}
	
	public static IQuestAction newAction(String type) {
		try {
			return actionTypes.get(type).newInstance();
		} catch(Exception e) { }
		return null;
	}
}