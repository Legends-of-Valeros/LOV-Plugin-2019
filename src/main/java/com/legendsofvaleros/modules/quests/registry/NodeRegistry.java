package com.legendsofvaleros.modules.quests.registry;

import com.legendsofvaleros.modules.quests.api.IQuestNode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NodeRegistry {
    private Map<String, Class<? extends IQuestNode>> types = new HashMap<>();
    private static final HashMap<Object, Type> instanceType = new HashMap<>();

    public Optional<Class<? extends IQuestNode>> getType(String id) {
        return Optional.ofNullable(types.get(id));
    }

    public boolean hasType(String id) {
        return getType(id).isPresent();
    }

    public void addType(String id, Class<? extends IQuestNode> type) {
        types.put(id, type);

        instanceType.put(id, ((ParameterizedType)type.getGenericSuperclass()).getActualTypeArguments()[0]);
        instanceType.put(type, ((ParameterizedType)type.getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public static Type getInstanceType(Object key) {
        return instanceType.get(key);
    }
}