package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class IInportTrigger<T> implements INodeInput<IOutportTrigger<?>> {
    final IQuestNode<T> node;

    IOutportTrigger<?> port;

    final INodeRunnable<T> runnable;

    public IInportTrigger(IQuestNode<T> node) {
        this.node = node;
        this.runnable = INodeRunnable.NOTHING;
    }

    public IInportTrigger(IQuestNode<T> node, @NotNull INodeRunnable<T> runnable) {
        this.node = node;
        this.runnable = runnable;
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
        // Respect inactive quests
        if(!instance.getState().isActive()) {
            return;
        }

        this.runnable.run(instance, instance.getNodeInstance(node));
    }
}