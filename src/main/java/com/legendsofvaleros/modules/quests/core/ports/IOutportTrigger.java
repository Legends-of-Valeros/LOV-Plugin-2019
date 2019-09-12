package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;
import com.legendsofvaleros.modules.quests.api.ports.INodeRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class IOutportTrigger<T> implements INodeOutput<IInportTrigger<?>> {
    final IQuestNode<T> node;

    final Set<IInportTrigger<?>> ports;

    final INodeRunnable<T> runnable;

    public IOutportTrigger(IQuestNode<T> node) {
        this.node = node;
        this.runnable = INodeRunnable.NOTHING;

        this.ports = new HashSet<>();
    }

    public IOutportTrigger(IQuestNode<T> node, @NotNull INodeRunnable runnable) {
        this.node = node;
        this.runnable = runnable;

        this.ports = new HashSet<>();
    }

    @Override
    public void addConnection(IInportTrigger<?> port) {
        this.ports.add(port);
    }

    @Override
    public Set<IInportTrigger<?>> getConnected() {
        return this.ports;
    }

    public void run(IQuestInstance instance) {
        this.runnable.run(instance, instance.getNodeInstance(node));

        this.getConnected().stream().forEach(port -> port.run(instance));
    }
}