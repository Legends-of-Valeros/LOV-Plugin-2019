package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;

public class INodeInputTrigger<V> implements INodeInput<INodeOutputTrigger<V>> {
    INodeOutputTrigger<V> output;

    final INodeRunnable runnable;

    public INodeInputTrigger(INodeRunnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void setConnection(INodeOutputTrigger<V> output) {
        this.output = output;
    }

    @Override
    public INodeOutputTrigger<V> getConnected() {
        return output;
    }

    public void run(IQuestInstance quest) {
        this.runnable.run(quest);
    }
}