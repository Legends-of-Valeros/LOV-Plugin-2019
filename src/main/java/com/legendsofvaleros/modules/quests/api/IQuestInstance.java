package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

import java.util.Set;

/**
 * A quest instance is created for each player with the quest currently active, and is
 * destroyed when the quest is completed or unloaded.
 */
public interface IQuestInstance {
    /**
     * Returns the quest that this object applies to.
     */
    IQuest getQuest();

    /**
     * Returns the player that this object applies to.
     */
    PlayerCharacter getPlayerCharacter();

    /**
     * Proxies a request to the quest to return all available nodes.
     */
    Set<INode<?>> getNodes();

    <T> T getData(INode<T> node);

    <T> void setData(INode<T> node, T data);
}