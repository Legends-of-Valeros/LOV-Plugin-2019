package com.legendsofvaleros.modules.quests.prerequisite;

import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuestPrerequisite;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class PrerequisiteFactory {
    private static HashMap<String, Class<? extends IQuestPrerequisite>> progressTypes = new HashMap<>();

    public static Set<String> getTypes() {
        return progressTypes.keySet();
    }

    public static Class<? extends IQuestPrerequisite> getType(String type) {
        return progressTypes.get(type);
    }

    public static String getType(IQuestPrerequisite prog) {
        for (Entry<String, Class<? extends IQuestPrerequisite>> entry : progressTypes.entrySet()) {
            if (entry.getValue().isAssignableFrom(prog.getClass()))
                return entry.getKey();
        }
        return null;
    }

    public static void registerType(String type, Class<? extends IQuestPrerequisite> objClass) {
        QuestController.getInstance().getLogger().finest("Registered prerequisite: " + type);
        progressTypes.put(type, objClass);
    }
}