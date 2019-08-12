package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;

import java.util.HashSet;
import java.util.Set;

public class INodeOutputTrigger<V> implements INodeOutput<INodeInputTrigger<V>> {
    Set<INodeInputTrigger<V>> inputs = new HashSet<>();

    final INodeRunnable runnable;

    public INodeOutputTrigger(INodeRunnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void addConnection(INodeInputTrigger input) {
        this.inputs.add(input);
    }

    @Override
    public Set<INodeInputTrigger<V>> getConnected() {
        return inputs;
    }

    public void run(IQuestInstance quest) {
        this.runnable.run(quest);

        // Fire all of the ports connected to this output port
        inputs.stream().forEach(v -> v.run(quest));
    }
}