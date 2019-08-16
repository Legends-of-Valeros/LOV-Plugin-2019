package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;

import java.util.UUID;

public abstract class AbstractQuestNode<T> implements IQuestNode<T> {
    private final String id;

    private float[] position;

    public AbstractQuestNode(String id) {
        this.id = id;
    }

    @Override
    public String getID() {
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
    public void onWake() {

    }

    @Override
    public void onActivated(IQuestInstance instance, T data) {

    }

    @Override
    public void onDeactivated(IQuestInstance instance, T data) {

    }

    @Override
    public void onSleep() {

    }
}
