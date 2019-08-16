package com.legendsofvaleros.modules.queue;

import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 08/02/2019
 * @param <T> The type of the queue its for
 */
public class Queue<T> {

    private T t;

    public T get() {
        return this.t;
    }

    public void set(T t1) {
        this.t = t1;
    }

    private ArrayList<Player> queuedPlayers = new ArrayList<>();

    private int maxPlayers; //maximum players allowed to be in one game
    private int minPlayersRequired; // minimum players required for the queue to start

    private String queueName;


    public Queue(int minPlayersRequired) {
        this.minPlayersRequired = minPlayersRequired;
        this.queueName = get().getClass().getCanonicalName();
    }

    public List<Player> getQueuedPlayers() {
        return queuedPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayersRequired() {
        return minPlayersRequired;
    }

    public String getQueueName() {
        return queueName;
    }

    @EventHandler
    public void onPlayerUnloadEvent(PlayerCharacterLogoutEvent ev) {
        queuedPlayers.remove(ev.getPlayer());
    }

}
