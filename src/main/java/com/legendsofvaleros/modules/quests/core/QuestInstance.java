package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.*;

import java.util.Collection;
import java.util.Collections;

public class QuestInstance implements IQuestInstance {
    final PlayerCharacter player;

    final IQuest quest;

    private QuestState state = QuestState.INACTIVE;

    final QuestNodeInstanceMap nodes;

    public QuestInstance(PlayerCharacter player, IQuest quest) {
        this.quest = quest;
        this.player = player;

        this.nodes = new QuestNodeInstanceMap();
    }

    @Override
    public PlayerCharacter getPlayerCharacter() {
        return this.player;
    }

    @Override
    public IQuest getQuest() {
        return this.quest;
    }

    @Override
    public QuestState getState() {
        return state;
    }

    @Override
    public QuestNodeInstanceMap getNodeInstances() {
        return this.nodes;
    }

    public <T> void setNodeInstance(IQuestNode<T> node, T instance) {
        nodes.putInstance(node, instance);
    }

    @Override
    public <T> T getNodeInstance(IQuestNode<T> node) {
        if(!this.nodes.hasInstance(node))
            this.nodes.putInstance(node, node.newInstance());
        return this.nodes.getInstance(node);
    }

    @Override
    public void addHistory(IQuestHistory... event) {

    }

    @Override
    public Collection<IQuestHistory> getHistory() {
        return Collections.emptyList();
    }

    @Override
    public void resetNodes() {
        this.nodes.clear();
    }

    @Override
    public void setState(QuestState state) {
        if(this.state.isNextStateAllowed(state)) throw new IllegalStateException("Quest instance cannot be set to '" + state.name() + "' while currently in '" + this.state.name() + "'!");

        if(state.isActive()) {
            this.quest.onActivated(this);
        }else{
            this.quest.onDeactivated(this);
        }

        this.state = state;
    }

    @Override
    public void onActivated() {
        for(IQuestNode node : quest.getNodes())
            node.onActivated(this, this.nodes.getInstance(node));
    }

    @Override
    public void onDeactivated() {
        for(IQuestNode node : quest.getNodes())
            node.onDeactivated(this, this.nodes.getInstance(node));
    }
}
