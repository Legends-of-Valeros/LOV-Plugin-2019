package com.legendsofvaleros.modules.quests.api;

import java.util.Arrays;

public enum QuestState {
    /**
     * This state is set when the quest has never been accepted, before.
     */
    INACTIVE(false, false),

    /**
     * This state is set when the quest is currently active.
     */
    ACTIVE(true, false),

    /**
     * This state is set when the quest was completed successfully.
     */
    SUCCESS(false, true),

    /**
     * This state is set when the quest was not completed successfully.
     */
    FAILED(false, true),

    /**
     * This state is set when the quest was abandoned.
     */
    ABANDONED(false, false);

    private final boolean isActive;
    private final boolean wasCompleted;
    private final QuestState[] allowedFrom;

    QuestState(boolean isActive, boolean wasCompleted, QuestState...allowedFrom) {
        this.isActive = isActive;
        this.wasCompleted = wasCompleted;
        this.allowedFrom = allowedFrom;
    }

    public boolean isActive() { return isActive; }

    public boolean wasCompleted() {
        return wasCompleted;
    }

    public boolean isNextStateAllowed(QuestState next) {
        // Don't allow setting the same state.
        if(this == next) return false;

        // Never allow returning to the INACTIVE state.
        if(next == INACTIVE) return false;

        // A quest can only switch states from an active state to an inactive state, or from an inactive to an active state.
        return this.isActive != next.isActive;
    }
}