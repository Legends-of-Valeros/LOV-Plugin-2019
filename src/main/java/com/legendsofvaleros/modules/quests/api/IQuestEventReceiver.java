package com.legendsofvaleros.modules.quests.api;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.event.Event;

public interface IQuestEventReceiver {
    /**
     * @return The bukkit events to receive in onEvent
     */
    Class<? extends Event>[] getRequestedEvents();

    void onEvent(Event event, PlayerCharacter pc);
}