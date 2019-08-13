package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.INode;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;
import com.legendsofvaleros.modules.quests.api.ports.INodeRunnable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class INodeOutputTrigger<V> implements INodeOutput<INodeInputTrigger<V>> {
    final INode node;

    final Set<INodeInputTrigger<V>> ports;

    final Optional<INodeRunnable> runnable;

    public INodeOutputTrigger(INode node) {
        this.node = node;
        this.runnable = Optional.empty();

        this.ports = new HashSet<>();
    }

    public INodeOutputTrigger(INode node, INodeRunnable runnable) {
        this.node = node;
        this.runnable = Optional.of(runnable);

        this.ports = new HashSet<>();
    }

    @Override
    public void addConnection(INodeInputTrigger<V> port) {
        this.ports.add(port);
    }

    @Override
    public Set<INodeInputTrigger<V>> getConnected() {
        return this.ports;
    }

    public void run() {
        this.runnable.ifPresent(INodeRunnable::run);

        this.getConnected().stream().forEach(INodeInputTrigger::run);
    }
}