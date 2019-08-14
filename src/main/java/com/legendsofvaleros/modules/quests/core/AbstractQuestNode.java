package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;

import java.util.UUID;

public abstract class AbstractQuestNode<T> implements IQuestNode<T> {
    private final UUID id;

    private float[] position;

    public AbstractQuestNode(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getID() {
        return id;
    }

    @Override
    public void setPosition(float x, float y) {
        this.position = new float[] { x, y };
    }

    @Override
    public float[] getPosition() {
        return position;
    }

    @Override
    public void onSetup(IQuestInstance quest, T instance) {

    }

    @Override
    public void onPause(IQuestInstance quest, T instance) {

    }

    @Override
    public void onResume(IQuestInstance quest, T instance) {

    }

    @Override
    public void onTeardown(IQuestInstance quest, T instance) {

    }
}
