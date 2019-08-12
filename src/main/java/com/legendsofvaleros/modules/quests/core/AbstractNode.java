package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.quests.api.INode;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;

public class AbstractNode<T> implements INode<T> {
    @Override
    public void onSetup() {

    }

    @Override
    public void onLoad(IQuestInstance quest) {

    }

    @Override
    public void onUnload(IQuestInstance quest) {

    }

    @Override
    public void onTeardown() {

    }
}
