package com.legendsofvaleros.modules.quests.api;

/**
 * The T generic parameter, here, is the data storage object. Use it to store quest data in the node.
 *
 * <br><br>
 *
 * Nodes should use their T type to store persistent data. Only use a field if persisting across logins or reboots
 * is not necessary.
 *
 * <br><br>
 *
 * Any included functions that are annotated using bukkit's @EventHandler must have IQuestInstance as
 * its first parameter. You do not need to register the events with bukkit, only include them in the
 * node definition.
 */
public interface INode<T> {
    /**
     * Fired when the node first created. i.e. when a quest is built in memory. Use this to init data or create
     * repeating tasks if you so desire.
     */
    void onSetup();

    /**
     * Fired when the node is loaded for a player. This includes quest is accepted, or loaded after a login.
     *
     * Here is where you should set up any handlers, tasks, etc for a quest that is started or resumed.
     */
    void onLoad(IQuestInstance quest);

    /**
     * Fired when the node is unloaded for a player. This includes quest is completed, or unloaded during a logouts.
     */
    void onUnload(IQuestInstance quest);

    /**
     * Fired when the node being deleted. i.e. when a quest is dumped from memory. Use this to delete data or stop
     * repeating tasks.
     */
    void onTeardown();
}