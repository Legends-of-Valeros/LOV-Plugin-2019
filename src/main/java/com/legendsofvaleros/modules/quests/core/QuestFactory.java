package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuest;

import java.util.HashMap;
import java.util.Set;

public class QuestFactory {
    private static HashMap<String, Class<? extends IQuest>> questTypes = new HashMap<>();
    public static Set<String> getTypes() {
        return questTypes.keySet();
    }
    public static Class<? extends IQuest> getType(String id) {
        return questTypes.get(id);
    }
    public static void registerType(String type, Class<? extends IQuest> objClass) {
        QuestController.getInstance().getLogger().finest("Registered quest type: " + type);
        questTypes.put(type, objClass);
    }
}