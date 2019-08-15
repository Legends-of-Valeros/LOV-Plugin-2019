package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface INodeReturn<T, V> {
    @NotNull V run(IQuestInstance instance, T data);
}