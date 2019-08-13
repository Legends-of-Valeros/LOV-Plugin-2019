package com.legendsofvaleros.modules.quests.core.ports;

import com.legendsofvaleros.modules.quests.api.INode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;

import java.util.Optional;

public class INodeInputValue<V> implements INodeInput<INodeOutputValue<V>> {
    final INode node;

    INodeOutputValue<V> port;

    public INodeInputValue(INode node) {
        this.node = node;
    }

    @Override
    public void setConnection(INodeOutputValue<V> port) {
        this.port = port;
    }

    @Override
    public Optional<INodeOutputValue<V>> getConnected() {
        return Optional.ofNullable(this.port);
    }

    public Optional<V> get() {
        Optional<INodeOutputValue<V>> conn = this.getConnected();

        return conn.isPresent() ? conn.get().get() : Optional.empty();
    }
}