package com.legendsofvaleros.modules.quests.registry;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.quests.QuestController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.*;

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
        Player[] getPlayers(T event);
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

            if(eh == null) {
                eh = attemptCreateHandler(c);
            }

            // Cache the class match to the handler list.
            handlers.put(c, eh);
        }

        return handlers.get(c) != null ? Optional.of(handlers.get(c)) : Optional.empty();
    }

    public boolean hasHandler(Class<? extends Event> c) {
        return getHandler(c).isPresent();
    }

    public <T extends Event> void addHandler(Class<T> c, IQuestEventHandler<T> handler) {
        Bukkit.getServer().getPluginManager().registerEvent(c, this, EventPriority.MONITOR, (listener, event) -> {
            Player[] ps = handler.getPlayers(c.cast(event));
            if(ps == null) return;

            for(Player p : ps) {
                if (!Characters.isPlayerCharacterLoaded(p)) return;

                QuestController.getInstance().propagateEvent(Characters.getPlayerCharacter(p), c, event);
            }
        }, LegendsOfValeros.getInstance());

        handlers.put(c, handler);
    }

    private IQuestEventHandler attemptCreateHandler(Class<? extends Event> c) {
        List<Method> methods = new ArrayList<>();

        for(Method m : c.getMethods()) {
            if(m.getParameterTypes().length != 0) continue;

            if(m.getReturnType() == Player.class
                || m.getReturnType() == PlayerCharacter.class
                || m.getReturnType() == CombatEntity.class) {
                methods.add(m);
            }
        }

        if(methods.size() == 0)
            return null;

        IQuestEventHandler handler = (event) -> {
            Set<Player> players = new HashSet<>();

            methods.forEach(m -> {
                try {
                    Object r = m.invoke(event);

                    if(r instanceof Player)
                        players.add((Player)r);
                    else if(r instanceof PlayerCharacter)
                        players.add(((PlayerCharacter)r).getPlayer());
                    else if(r instanceof CombatEntity) {
                        if(((CombatEntity)r).isPlayer())
                            players.add((Player)((CombatEntity)r).getLivingEntity());
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });

            return players.toArray(new Player[0]);
        };

        addHandler(c, handler);

        return handler;
    }
}