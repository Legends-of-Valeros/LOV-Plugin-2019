package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.quests.api.IQuestNode;

import java.util.HashMap;

public class QuestNodeInstanceMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public boolean hasInstance(IQuestNode node) {
        return containsKey(node.getId());
    }

    public <T> void putInstance(IQuestNode<T> node, T obj) {
        put(node.getId(), obj);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(IQuestNode<T> node) {
        return (T)get(node.getId());
    }
}
