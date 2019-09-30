package com.legendsofvaleros.modules.queue.events;

import com.legendsofvaleros.modules.queue.Queue;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Crystall on 08/04/2019
 * An event that is called when the queue has enough players and is about to finish and starts the new mechanic
 */
public class QueueReadyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private List<Player> players;
    private Queue queue;

    public QueueReadyEvent(List<Player> players, Queue queue) {
        this.players = players;
        this.queue = queue;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Queue getQueue() {
        return queue;
    }
}
