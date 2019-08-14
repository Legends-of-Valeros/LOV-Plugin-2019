package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.core.QuestInstance;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IQuest {
    /**
     * @return The unique name used to identify this gear.
     */
    String getId();
    /**
     * @return The unique slug used to identify this gear with a human readable identifier.
     */
    String getSlug();

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
    IQuestPrerequisite[] getPrerequisites();

    /**
     * @return The options that define how a quest can be repeated.
     */
    Object getRepeatOptions();

    Optional<IQuestNode> getNode(UUID id);

    Collection<IQuestNode> getNodes();

    Map<IQuestNode, Method> getListeners(Class<? extends Event> caught);

    QuestStatus getStatus(PlayerCharacter pc);

    /**
     * Returns a quest instance for the player. This should always return a value, even if the player doesn't have an
     * instance tracked. However, if a new instance is created, it should NOT be tracked.
     */
    IQuestInstance getInstance(PlayerCharacter pc);

    /**
     * Adds the instance to the quest. This is called on anything ranging from quest acceptance to just a log in.
     */
    void setInstance(CharacterId characterId, IQuestInstance instance);

    /**
     * Remove the instance from the quest. This is called on anything ranging from quest deletion to just a log out.
     */
    void removeInstance(CharacterId characterId);

    /**
     * Fired when a quest's nodes are to be made active for an instance. This may be for any reason between creation, or
     * the instance being loaded after a login.
     */
    void onActivated(IQuestInstance instance);

    /**
     * Fired when a quest's nodes are to be deactivated for an instance. This may be for any reason between logout out,
     * or the instance being deleted entirely.
     */
    void onDeactivated(IQuestInstance instance);

    void callEvent(IQuestInstance instance, Class<? extends Event> caught, Event event);
}