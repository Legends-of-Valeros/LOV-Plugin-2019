package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeRunnable;

import java.util.Optional;

public class INodeInputTrigger<V> implements INodeInput<INodeOutputTrigger<V>> {
    final IQuestNode node;

    INodeOutputTrigger<V> port;

    final Optional<INodeRunnable> runnable;

    public INodeInputTrigger(IQuestNode node) {
        this.node = node;
        this.runnable = Optional.empty();
    }

    public INodeInputTrigger(IQuestNode node, INodeRunnable runnable) {
        this.node = node;
        this.runnable = Optional.of(runnable);
    }

    @Override
    public void setConnection(INodeOutputTrigger<V> port) {
        this.port = port;
    }

    @Override
    public Optional<INodeOutputTrigger<V>> getConnected() {
        return Optional.ofNullable(this.port);
    }

    public void run() {
        this.runnable.ifPresent(INodeRunnable::run);
    }
}