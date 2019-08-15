package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeRunnable;

import java.util.Optional;

public class IInportTrigger<T> implements INodeInput<IOutportTrigger<?>> {
    final IQuestNode<T> node;

    IOutportTrigger<?> port;

    final Optional<INodeRunnable<T>> runnable;

    public IInportTrigger(IQuestNode<T> node) {
        this.node = node;
        this.runnable = Optional.empty();
    }

    public IInportTrigger(IQuestNode<T> node, INodeRunnable<T> runnable) {
        this.node = node;
        this.runnable = Optional.of(runnable);
    }

    @Override
    public void setConnection(IOutportTrigger<?> port) {
        this.port = port;
    }

    @Override
    public Optional<IOutportTrigger<?>> getConnected() {
        return Optional.ofNullable(this.port);
    }

    public void run(IQuestInstance instance) {
        this.runnable.ifPresent(run -> run.run(instance, instance.getNodeInstance(node)));
    }
}