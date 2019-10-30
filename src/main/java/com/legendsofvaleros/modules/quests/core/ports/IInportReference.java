package com.legendsofvaleros.modules.quests.core.ports;

import com.google.gson.reflect.TypeToken;
import com.legendsofvaleros.api.Ref;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;

import java.util.Optional;

/**
 * With references, the default value is always null.
 */
public class IInportReference<T, V> extends IInportValue<T, Ref<V>> {
    public IInportReference(IQuestNode<T> node, TypeToken<Ref<V>> valClass) {
        super(node, valClass, null);
    }

    public Optional<V> get(IQuestInstance instance) {
        Optional<IOutportValue<?, Ref<V>>> conn = this.getConnected();

        V val = (conn.isPresent() ? conn.get().get(instance).get() : (defaultValue != null ? defaultValue.get() : null));

        return val != null ? Optional.of(val) : Optional.empty();
    }
}