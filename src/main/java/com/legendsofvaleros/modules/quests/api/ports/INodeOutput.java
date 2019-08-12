package com.legendsofvaleros.modules.quests.api.ports;

import java.util.Set;

public interface INodeOutput<T extends INodeInput> {
    /**
     * This is an internal function and should never be used during a quest.
     */
    void addConnection(T output);

    /**
     * Returns the node ports connected to this port.
     */
    Set<T> getConnected();
}