package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;

import java.util.Optional;
import java.util.Set;

public class INodeInputValue<T, V> implements INodeInput<INodeOutputValue<?, V>> {
    final IQuestNode<T> node;

    Class<V> valClass;
    V defaultValue;
    INodeOutputValue<?, V> port;

    public INodeInputValue(Class<V> valClass, IQuestNode<T> node) {
        this.valClass = valClass;
        this.node = node;
    }

    public Class<V> getValueClass() {
        return valClass;
    }

    public void setDefaultValue(V defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void setConnection(INodeOutputValue<?, V> port) {
        this.port = port;
    }

    @Override
    public Optional<INodeOutputValue<?, V>> getConnected() {
        return Optional.ofNullable(this.port);
    }

    public Optional<V> get(IQuestInstance instance) {
        Optional<INodeOutputValue<?, V>> conn = this.getConnected();

        return conn.isPresent() ? conn.get().get(instance) : Optional.ofNullable(defaultValue);
    }
}