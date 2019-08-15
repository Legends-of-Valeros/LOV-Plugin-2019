package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeRunnable;

import java.util.Optional;

public class INodeInputTrigger<T> implements INodeInput<INodeOutputTrigger<?>> {
    final IQuestNode<T> node;

    INodeOutputTrigger<?> port;

    final Optional<INodeRunnable<T>> runnable;

    public INodeInputTrigger(IQuestNode<T> node) {
        this.node = node;
        this.runnable = Optional.empty();
    }

    public INodeInputTrigger(IQuestNode<T> node, INodeRunnable<T> runnable) {
        this.node = node;
        this.runnable = Optional.of(runnable);
    }

    @Override
    public void setConnection(INodeOutputTrigger<?> port) {
        this.port = port;
    }

    @Override
    public Optional<INodeOutputTrigger<?>> getConnected() {
        return Optional.ofNullable(this.port);
    }

    public void run(IQuestInstance instance) {
        this.runnable.ifPresent(run -> run.run(instance, instance.getNodeInstance(node)));
    }
}