package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.INode;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;

public class AbstractNode implements INode {
    final IQuestInstance quest;

    public AbstractNode(IQuestInstance quest) {
        this.quest = quest;
    }

    @Override
    public IQuestInstance getInstance() {
        return quest;
    }

    @Override
    public PlayerCharacter getPlayerCharacter() {
        return quest.getPlayerCharacter();
    }

    @Override
    public void onSetup() {

    }

    @Override
    public void onTeardown() {

    }
}
