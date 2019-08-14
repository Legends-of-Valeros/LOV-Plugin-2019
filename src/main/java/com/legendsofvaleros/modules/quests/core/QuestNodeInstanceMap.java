package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.quests.api.IQuestNode;

import java.util.HashMap;
import java.util.UUID;

public class QuestNodeInstanceMap extends HashMap<UUID, Object> {
    private static final long serialVersionUID = 1L;

    public boolean hasInstance(IQuestNode node) {
        return containsKey(node.getID());
    }

    public <T> void putInstance(IQuestNode<T> node, T obj) {
        put(node.getID(), obj);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(IQuestNode<T> node) {
        return (T)get(node.getID());
    }
}
