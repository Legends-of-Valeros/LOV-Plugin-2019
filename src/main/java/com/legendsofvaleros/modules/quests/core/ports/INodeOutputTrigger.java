package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;
import com.legendsofvaleros.modules.quests.api.ports.INodeRunnable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class INodeOutputTrigger<T> implements INodeOutput<INodeInputTrigger<?>> {
    final IQuestNode<T> node;

    final Set<INodeInputTrigger<?>> ports;

    final Optional<INodeRunnable<T>> runnable;

    public INodeOutputTrigger(IQuestNode<T> node) {
        this.node = node;
        this.runnable = Optional.empty();

        this.ports = new HashSet<>();
    }

    public INodeOutputTrigger(IQuestNode<T> node, INodeRunnable runnable) {
        this.node = node;
        this.runnable = Optional.of(runnable);

        this.ports = new HashSet<>();
    }

    @Override
    public void addConnection(INodeInputTrigger<?> port) {
        this.ports.add(port);
    }

    @Override
    public Set<INodeInputTrigger<?>> getConnected() {
        return this.ports;
    }

    public void run(IQuestInstance instance) {
        this.runnable.ifPresent(run -> run.run(instance, instance.getNodeInstance(node)));

        this.getConnected().stream().forEach(port -> port.run(instance));
    }
}