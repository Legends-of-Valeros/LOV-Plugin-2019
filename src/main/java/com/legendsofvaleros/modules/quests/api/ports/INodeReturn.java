package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;

import java.util.Optional;

public interface INodeReturn<T, V> {
    Optional<V> run(IQuestInstance instance, T data);
}