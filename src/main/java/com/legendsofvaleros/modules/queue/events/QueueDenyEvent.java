package com.legendsofvaleros.modules.queue.events;

import com.legendsofvaleros.modules.queue.Queue;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * Created by Crystall on 08/02/2019
 * Gets fired whenever a player is denying a queue accept request
 */
public class QueueDenyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private Queue queue;

    public QueueDenyEvent(Player player, Queue queue) {
        this.player = player;
        this.queue = queue;
    }

    public HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Queue getQueue() {
        return queue;
    }

}
