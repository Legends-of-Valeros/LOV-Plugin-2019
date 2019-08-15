package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.core.QuestLogEntry;
import com.legendsofvaleros.modules.quests.core.QuestNodeInstanceMap;

import java.util.*;

/**
 * A quest instance is created for each player with the quest currently active, completed, failed, or abandoned. It does
 * not exist if and only if the player has never accepted the quest.
 */
public interface IQuestInstance {
    /**
     * Returns the player that this object applies to.
     */
    PlayerCharacter getPlayerCharacter();

    /**
     * Returns the quest that this object applies to.
     */
    IQuest getQuest();

    /**
     * Returns the current state of the quest. In general, if a quest is inactive, it can be accepted. However, for
     * repeating quests, this state is largely ignored.
     */
    QuestState getState();

    QuestNodeInstanceMap getNodeInstances();

    /**
     * Should never return null. If a node does not yet have an instance, then it should be created when queried.
     */
    <T> T getNodeInstance(IQuestNode<T> node);

    <T> void setNodeInstance(IQuestNode<T> node, T instance);

    Map<Integer, QuestLogEntry> getLogEntries();

    /**
     * Returns an int identifier for the log entry. This is to be used to edit the state of the entry.
     */
    int addLogEntry(QuestLogEntry entry);

    /**
     * This updates the log entry. We don't allow editing directly so that we can fire an event when the log is updated.
     */
    void updateLogEntry(int id, QuestLogEntry entry);

    /**
     * This should return a copy of the log entry.
     */
    Optional<QuestLogEntry> getLogEntry(int id);

    void removeLogEntry(int id);

    void addHistory(IQuestHistory... event);

    Collection<IQuestHistory> getHistory();

    /**
     * Deletes all instancing data.
     */
    void reset();

    void setState(QuestState state) throws IllegalStateException;

    void onActivated();

    void onDeactivated();
}