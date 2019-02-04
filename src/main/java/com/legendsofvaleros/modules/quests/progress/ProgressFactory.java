package com.legendsofvaleros.modules.quests.progress;

import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuestObjectiveProgress;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ProgressFactory {
    private static HashMap<String, Class<? extends IQuestObjectiveProgress>> progressTypes = new HashMap<>();

    public static Set<String> getTypes() {
        return progressTypes.keySet();
    }

    public static String getType(IQuestObjectiveProgress prog) {
        for (Entry<String, Class<? extends IQuestObjectiveProgress>> entry : progressTypes.entrySet()) {
            if (entry.getValue().isAssignableFrom(prog.getClass()))
                return entry.getKey();
        }
        return null;
    }

    public static void registerType(String type, Class<? extends IQuestObjectiveProgress> objClass) {
        QuestController.getInstance().getLogger().finest("Registered progress type: " + type);
        progressTypes.put(type, objClass);
    }

    public static IQuestObjectiveProgress forType(String type) {
        try {
            return progressTypes.get(type).newInstance();
        } catch (Exception e) {
        }
        return null;
    }
}