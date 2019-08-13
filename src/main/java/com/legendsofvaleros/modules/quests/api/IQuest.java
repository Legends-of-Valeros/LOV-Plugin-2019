package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.core.QuestInstance;
import com.legendsofvaleros.modules.questsold.api.IQuestPrerequisite;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IQuest {
    /**
     * @return The unique name used to identify this gear.
     */
    String getId();

    /**
     * @return The display name to show in UIs for the player.
     */
    String getName();

    /**
     * @return A description to show for the quest in the quest book.
     */
    String getDescription();

    boolean isForced();

    /**
     * @return The list of prerequisite quests that must be completed before this one is available.
     */
    List<IQuestPrerequisite> getPrerequisites();

    /**
     * @return The options that define how a quest can be repeated.
     */
    void getRepeatOptions();

    /**
     * @return A map of from Node output UUIDs to Node input UUIDs.
     */
    Map<UUID, UUID> getConnections();

    void newInstance(PlayerCharacter player);

    void loadInstance(PlayerCharacter player, QuestInstance instance);

    Optional<IQuestInstance> getInstance(PlayerCharacter player);
}