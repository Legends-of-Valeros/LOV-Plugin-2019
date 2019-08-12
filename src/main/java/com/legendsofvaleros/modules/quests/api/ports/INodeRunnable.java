package com.legendsofvaleros.modules.quests.api.ports;

import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;

public interface INodeRunnable {
    void run(IQuestInstance quest);
}