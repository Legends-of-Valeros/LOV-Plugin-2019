package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;

public interface INodeRunnable<T> {
    void run(IQuestInstance instance, T data);
}