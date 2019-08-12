package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;

import java.util.Optional;

public class INodeInputValue<V> implements INodeInput<INodeOutputValue<V>> {
    INodeOutputValue<V> output;

    public INodeInputValue() { }

    @Override
    public void setConnection(INodeOutputValue<V> output) {
        this.output = output;
    }

    @Override
    public INodeOutputValue<V> getConnected() {
        return output;
    }

    public Optional<V> get(IQuestInstance quest) {
           return this.output.get(quest);
    }
}