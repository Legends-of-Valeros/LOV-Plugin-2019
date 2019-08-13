package com.legendsofvaleros.modules.quests.registry;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Used by quests to filter the player object from events so the quest system can route events to the
 * correct nodes. Note that registered events are inheritance-aware; registering a PlayerEvent handler
 * will be able to handle player objects from any object extending.
 * <p>
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
        if (!handlers.containsKey(c)) {
            Optional<IQuestEventHandler> eh = Optional.empty();

            Class<?> sc = c;
            while((sc = sc.getSuperclass()) != null) {
                if(handlers.containsKey(sc)) {
                    eh = handlers.get(sc);
                    break;
                }
            }

            // Cache the class match to the handler list.
            handlers.put(c, eh);
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