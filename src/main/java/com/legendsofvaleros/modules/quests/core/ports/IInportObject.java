package com.legendsofvaleros.modules.quests.core.ports;

import com.google.gson.reflect.TypeToken;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class IInportObject<T, V> extends IInportValue<T, V> {
    public IInportObject(IQuestNode<T> node, TypeToken<V> valClass, @NotNull V defaultValue) {
        super(node, valClass, defaultValue);
    }

    public V get(IQuestInstance instance) {
        Optional<IOutportValue<?, V>> conn = this.getConnected();

        return conn.isPresent() ? conn.get().get(instance) : defaultValue;
    }
}