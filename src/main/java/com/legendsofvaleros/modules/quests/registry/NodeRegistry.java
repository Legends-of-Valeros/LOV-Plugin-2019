package com.legendsofvaleros.modules.quests.registry;

import com.legendsofvaleros.modules.quests.api.INode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NodeRegistry {
    private Map<String, Class<INode>> types = new HashMap<>();

    public Optional<Class<INode>> getType(String id) {
        return Optional.ofNullable(types.get(id));
    }

    public boolean hasType(String id) {
        return getType(id).isPresent();
    }

    public void addType(String id, Class<INode> type) {
        types.put(id, type);
    }

    public Optional<INode> createNode(String id) {
        if(!hasType(id)) return Optional.empty();

        // TODO: Create node object

        return null;
    }

    public <T> Optional<T> loadNodeData(INode<T> _, String data) {
        // TODO: Decode data into T

        return null;
    }
}