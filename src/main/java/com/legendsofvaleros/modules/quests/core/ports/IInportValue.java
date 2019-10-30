package com.legendsofvaleros.modules.quests.core.ports;

import com.google.gson.reflect.TypeToken;
import com.legendsofvaleros.api.Ref;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;

import java.util.Optional;

public abstract class IInportValue<T, V> implements INodeInput<IOutportValue<?, V>> {
    public static <T, V> IInportObject<T, V> of(IQuestNode<T> node, Class<V> valClass, V defaultValue) {
        return new IInportObject(node, TypeToken.get(valClass), defaultValue);
    }

    public static <T, V> IInportReference<T, V> ref(IQuestNode<T> node, Class<V> refClass) {
        return new IInportReference(node, TypeToken.getParameterized(Ref.class, refClass));
    }

    final IQuestNode<T> node;

    TypeToken<V> valClass;
    V defaultValue;
    IOutportValue<?, V> port;

    public IInportValue(IQuestNode<T> node, TypeToken<V> valClass, V defaultValue) {
        this.node = node;

        this.valClass = valClass;
        this.defaultValue = defaultValue;
    }

    public TypeToken<V> getValueClass() {
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
}