package com.legendsofvaleros.modules.quests.api.ports;

import java.util.Optional;

public interface INodeReturn<V> {
    Optional<V> run();
}