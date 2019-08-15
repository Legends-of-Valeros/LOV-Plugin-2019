package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.core.QuestNodeInstanceMap;
import org.bukkit.event.Event;

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

    void addHistory(IQuestHistory... event);

    Collection<IQuestHistory> getHistory();

    /**
     * Deletes all node data.
     */
    void resetNodes();

    void setState(QuestState state);

    void onActivated();

    void onDeactivated();
}