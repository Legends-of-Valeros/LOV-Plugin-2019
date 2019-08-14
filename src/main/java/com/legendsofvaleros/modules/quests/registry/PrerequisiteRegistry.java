package com.legendsofvaleros.modules.quests.registry;

import com.legendsofvaleros.modules.quests.api.IQuestPrerequisite;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PrerequisiteRegistry {
    private Map<String, Class<? extends IQuestPrerequisite>> types = new HashMap<>();

    public Optional<Class<? extends IQuestPrerequisite>> getType(String id) {
        return Optional.ofNullable(types.get(id));
    }

    public boolean hasType(String id) {
        return getType(id).isPresent();
    }

    public void addType(String id, Class<? extends IQuestPrerequisite> type) {
        types.put(id, type);
    }
}