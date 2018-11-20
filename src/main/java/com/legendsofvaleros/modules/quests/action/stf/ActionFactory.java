package com.legendsofvaleros.modules.quests.action.stf;

import com.legendsofvaleros.modules.quests.Quests;

import java.util.HashMap;
import java.util.Set;

public class ActionFactory {
	private static final HashMap<String, Class<? extends AbstractAction>> actionTypes = new HashMap<>();
	public static Set<String> getTypes() { return actionTypes.keySet(); }

	public static void registerType(String type, Class<? extends AbstractAction> objClass) {
		Quests.getInstance().getLogger().finest("Registered action: " + type);
		actionTypes.put(type, objClass);
	}
	
	public static AbstractAction newAction(String type) {
		try {
			return actionTypes.get(type).newInstance();
		} catch(Exception e) { }
		return null;
	}
}