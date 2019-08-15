package com.legendsofvaleros.modules.quests.api;

import java.util.UUID;

/**
 * Quest nodes use a form of the flyweight pattern. Hence, this object is instantiated once for every quest node. This
 * is mainly a configuration store, and doesn't yet contain any real functions. All function calls are held within a
 * node's instance for ease of use.
 * <p/>
 * The generic T defines the instance storage object for the node.
 */
public interface IQuestNode<T> {
    UUID getID();

    T newInstance();

    void setPosition(float x, float y);

    /**
     * Position is used in some cases where order of nodes matters. For example, if an objective node is above another, it
     * will be displayed above it in the quest log.
     */
    float[] getPosition();

    /**
     * Fired when the first instance of a quest is activated.
     */
    void onWake();

    /**
     * Fired when an instance of the quest is activated. This may be called when the quest is started, loaded after being
     * unloaded, or any other reason. However, it will not be called more than once, unless it is deactivated, first.
     */
    void onActivated(IQuestInstance instance, T data);

    /**
     * Fired when an instance of the quest is deactivated. This may be called when the quest is completed, failed, abandoned,
     * unloaded after being loaded, or any other reason. However, it will not be called more than once, unless it is
     * activated, first.
     */
    void onDeactivated(IQuestInstance instance, T data);

    /**
     * Fired when the last instance of a quest is deactivated.
     */
    void onSleep();
}