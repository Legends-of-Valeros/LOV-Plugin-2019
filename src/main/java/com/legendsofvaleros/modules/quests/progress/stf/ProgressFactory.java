package com.legendsofvaleros.modules.quests.progress.stf;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.quests.Quests;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ProgressFactory {
    private static HashMap<String, Class<? extends IObjectiveProgress>> progressTypes = new HashMap<>();

    public static Set<String> getTypes() {
        return progressTypes.keySet();
    }

    public static String getType(IObjectiveProgress prog) {
        for (Entry<String, Class<? extends IObjectiveProgress>> entry : progressTypes.entrySet()) {
            if (entry.getValue().isAssignableFrom(prog.getClass()))
                return entry.getKey();
        }
        return null;
    }

    public static void registerType(String type, Class<? extends IObjectiveProgress> objClass) {
        Quests.getInstance().getLogger().finest("Registered progress type: " + type);
        progressTypes.put(type, objClass);
    }

    public static IObjectiveProgress forType(String type) {
        try {
            return progressTypes.get(type).newInstance();
        } catch (Exception e) {
        }
        return null;
    }
}