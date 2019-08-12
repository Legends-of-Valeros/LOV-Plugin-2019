package com.legendsofvaleros.modules.regions.trigger;

import com.legendsofvaleros.modules.questsold.QuestController;
import com.legendsofvaleros.modules.regions.api.IRegionTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RegionTriggerFactory {
    private static HashMap<String, Class<? extends IRegionTrigger>> triggerTypes = new HashMap<>();

    public static Set<String> getTypes() {
        return triggerTypes.keySet();
    }

    public static Class<? extends IRegionTrigger> getType(String type) {
        return triggerTypes.get(type);
    }

    public static String getType(IRegionTrigger prog) {
        for (Map.Entry<String, Class<? extends IRegionTrigger>> entry : triggerTypes.entrySet()) {
            if (entry.getValue().isAssignableFrom(prog.getClass()))
                return entry.getKey();
        }
        return null;
    }

    public static void registerType(String type, Class<? extends IRegionTrigger> objClass) {
        QuestController.getInstance().getLogger().finest("Registered trigger: " + type);
        triggerTypes.put(type, objClass);
    }
}