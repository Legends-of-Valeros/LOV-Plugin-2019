package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;

import java.util.Optional;

public interface INodeReturn<V> {
    Optional<V> run(IQuestInstance quest);
}