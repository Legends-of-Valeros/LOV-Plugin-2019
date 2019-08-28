package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class IInportValue<T, V> implements INodeInput<IOutportValue<?, V>> {
    final IQuestNode<T> node;

    Class<V> valClass;
    V defaultValue;
    IOutportValue<?, V> port;

    public IInportValue(IQuestNode<T> node, Class<V> valClass, @NotNull V defaultValue) {
        this.node = node;

        this.valClass = valClass;
        this.defaultValue = defaultValue;
    }

    public Class<V> getValueClass() {
        return valClass;
    }

    public void setDefaultValue(V defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void setConnection(IOutportValue<?, V> port) {
        this.port = port;
    }

    @Override
    public Optional<IOutportValue<?, V>> getConnected() {
        return Optional.ofNullable(this.port);
    }

    public V get(IQuestInstance instance) {
        Optional<IOutportValue<?, V>> conn = this.getConnected();

        return conn.isPresent() ? conn.get().get(instance) : defaultValue;
    }
}