package com.legendsofvaleros.modules.quests.registry;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Used by quests to pull the player object from events so that nodes can use events more easily. Note
 * that registered events are inheritance-aware; registering a PlayerEvent handler will be able to
 * pull player objects from any object extending.
 *
 * <br><br>
 *
 * All things should be registered before the plugin is completely loaded, else the internal cache for extended
 * objects may be outdated.
 */
public class EventRegistry {
    @FunctionalInterface
    public interface IQuestEventHandler {
        Player getPlayer();
    }

    private Map<Class<?>, Optional<IQuestEventHandler>> handlers = new HashMap<>();

    public Optional<IQuestEventHandler> getHandler(Class<?> c) {
        // If no handler is explicitly defined, check the map for superclass matches.
        // TODO: Do we need to check for closest superclass match?
        if(!handlers.containsKey(c)) {
            Optional<IQuestEventHandler> eh = null;

            for(Map.Entry<Class<?>, Optional<IQuestEventHandler>> e : handlers.entrySet()) {
                if(e.getKey().isAssignableFrom(c)) {
                    eh = e.getValue();
                    break;
                }
            }

            if(eh != null)
                handlers.put(c, eh);
            else
                handlers.put(c, Optional.empty());
        }

        return handlers.get(c);
    }

    public boolean hasHandler(Class<?> c) {
        return getHandler(c).isPresent();
    }

    public void addHandler(Class<?> c, IQuestEventHandler handler) {
        handlers.put(c, Optional.ofNullable(handler));
    }
}