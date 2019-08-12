package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class INodeOutputValue<V> implements INodeOutput<INodeInputValue<V>> {
    Set<INodeInputValue<V>> output = new HashSet<>();

    final INodeReturn runnable;

    public INodeOutputValue(INodeReturn runnable) {
        this.runnable = runnable;
    }

    @Override
    public void addConnection(INodeInputValue<V> output) {
        this.output.add(output);
    }

    @Override
    public Set<INodeInputValue<V>> getConnected() {
        return output;
    }

    public Optional<V> get(IQuestInstance quest) {
        return this.runnable.run(quest);
    }
}