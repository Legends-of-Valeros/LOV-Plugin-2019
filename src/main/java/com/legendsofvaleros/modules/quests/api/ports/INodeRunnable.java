package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;

public interface INodeRunnable<T> {
    INodeRunnable NOTHING = (instance, data) -> { };

    void run(IQuestInstance instance, T data);
}