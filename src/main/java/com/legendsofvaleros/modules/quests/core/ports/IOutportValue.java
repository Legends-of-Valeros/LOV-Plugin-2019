package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;
import com.legendsofvaleros.modules.quests.api.ports.INodeReturn;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class IOutportValue<T, V> implements INodeOutput<IInportValue<?, V>> {
    final IQuestNode<T> node;

    Class<V> valClass;
    final Set<IInportValue<?, V>> ports;

    final INodeReturn<T, V> runnable;

    public IOutportValue(IQuestNode<T> node, Class<V> valClass, @NotNull INodeReturn<T, V> runnable) {
        this.node = node;

        this.valClass = valClass;
        this.runnable = runnable;

        this.ports = new HashSet<>();
    }

    @Override
    public void addConnection(IInportValue<?, V> port) {
        this.ports.add(port);
    }

    @Override
    public Set<IInportValue<?, V>> getConnected() {
        return this.ports;
    }

    public V get(IQuestInstance instance) {
        return this.runnable.run(instance, instance.getNodeInstance(node));
    }
}