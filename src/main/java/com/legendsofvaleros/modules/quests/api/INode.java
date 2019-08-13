package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

/**
 * Instantiated for every player who has started, or, in some cases, merely interacted with, the quest.
 * <p>
 * Any included functions that are annotated using bukkit's @EventHandler must have IQuestInstance as
 * its first parameter. You do not need to register the events with bukkit, only include them in the
 * node definition.
 */
public interface INode {
    /**
     * Returns the quest that this object applies to.
     */
    IQuestInstance getInstance();

    /**
     * Returns the player that this object applies to.
     */
    PlayerCharacter getPlayerCharacter();

    /**
     * Fired when the node first created. i.e. when a quest is built in memory. Use this to init data or create
     * repeating tasks if you so desire.
     */
    void onSetup();

    /**
     * Fired when the node being deleted. i.e. when a quest is dumped from memory. Use this to delete data or stop
     * repeating tasks.
     */
    void onTeardown();
}