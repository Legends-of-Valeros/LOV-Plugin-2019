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

    public static void registerType(String type, Class<? extends IQuest> objClass) {
        QuestController.getInstance().getLogger().finest("Registered gear type: " + type);
        questTypes.put(type, objClass);
    }

    public static IQuest newQuest(String id, String type) {
        try {
            return questTypes.get(type).getDeclaredConstructor(String.class).newInstance(id);
        } catch (Exception e) {
        }
        return null;
    }
}