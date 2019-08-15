package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;
import com.legendsofvaleros.modules.quests.api.ports.INodeReturn;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class INodeOutputValue<T, V> implements INodeOutput<INodeInputValue<?, V>> {
    final IQuestNode<T> node;

    final Set<INodeInputValue<?, V>> ports;

    final Optional<INodeReturn<T, V>> runnable;

    public INodeOutputValue(IQuestNode node) {
        this.node = node;
        this.runnable = Optional.empty();

        this.ports = new HashSet<>();
    }

    public INodeOutputValue(IQuestNode node, INodeReturn runnable) {
        this.node = node;
        this.runnable = Optional.of(runnable);

        this.ports = new HashSet<>();
    }

    @Override
    public void addConnection(INodeInputValue<?, V> port) {
        this.ports.add(port);
    }

    @Override
    public Set<INodeInputValue<?, V>> getConnected() {
        return this.ports;
    }

    public Optional<V> get(IQuestInstance instance) {
        return this.runnable.isPresent() ? this.runnable.get().run(instance, instance.getNodeInstance(node)) : Optional.empty();
    }
}