package com.legendsofvaleros.modules.quests.api.ports;

public interface INodeInput<T extends INodeOutput> {
    /**
     * This is an internal function and should never be used during a quest.
     */
    void setConnection(T output);

    /**
     * Returns the node ports connected to this port.
     */
    T getConnected();
}