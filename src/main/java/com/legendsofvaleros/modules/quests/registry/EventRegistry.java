package com.legendsofvaleros.modules.quests.registry;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.QuestController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Used by quests to filter the player object from events so the quest system can route events to the
 * correct nodes. Note that registered events are inheritance-aware; registering a PlayerEvent handler
 * will be able to handle player objects from any object extending. This also means if an extended class
 * is also registered here, the event will be caught twice.
 * <p>
 * All things should be registered before the plugin is completely loaded, else the internal cache for extended
 * objects may be outdated.
 */
public class EventRegistry implements Listener {
    @FunctionalInterface
    public interface IQuestEventHandler<T extends Event> {
        Player getPlayer(T event);
    }

    private Map<Class<?>, IQuestEventHandler> handlers = new HashMap<>();

    public <T extends Event> Optional<IQuestEventHandler<T>> getHandler(Class<T> c) {
        // If no handler is explicitly defined, check the map for superclass matches.
        if (!handlers.containsKey(c)) {
            IQuestEventHandler eh = null;

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

        return Optional.ofNullable(handlers.get(c));
    }

    public boolean hasHandler(Class<? extends Event> c) {
        return getHandler(c).isPresent();
    }

    public <T extends Event> void addHandler(Class<T> c, IQuestEventHandler<T> handler) {
        handlers.put(c, handler);

        Bukkit.getServer().getPluginManager().registerEvent(c, this, EventPriority.MONITOR, (listener, event) -> {
            Player p = handler.getPlayer(c.cast(event));
            if(!Characters.isPlayerCharacterLoaded(p)) return;

            QuestController.getInstance().propagateEvent(Characters.getPlayerCharacter(p), c, event);
        }, LegendsOfValeros.getInstance());
    }
}