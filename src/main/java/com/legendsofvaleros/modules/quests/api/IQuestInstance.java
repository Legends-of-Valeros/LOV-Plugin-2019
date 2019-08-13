package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    Map<UUID, INode> getNodeMap();

    Optional<INode> getNode(UUID uuid);
}