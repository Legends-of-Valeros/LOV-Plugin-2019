package com.legendsofvaleros.modules.quests.quest.stf;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.quests.Quests;

import java.util.HashMap;
import java.util.Set;

public class QuestFactory {
    private static HashMap<String, Class<? extends IQuest>> questTypes = new HashMap<>();

    public static Set<String> getTypes() {
        return questTypes.keySet();
    }

    public static void registerType(String type, Class<? extends IQuest> objClass) {
        Quests.getInstance().getLogger().finest("Registered quest type: " + type);
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