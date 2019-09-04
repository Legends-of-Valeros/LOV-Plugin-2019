package com.legendsofvaleros.modules.queue;

import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.queue.events.QueueReadyEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 08/02/2019
 * @param <T> The type of the queue its for
 */
public class Queue<T> implements Listener {

    private T t;

    public T get() {
        return this.t;
    }

    public void set(T t1) {
        this.t = t1;
    }

    private List<Player> queuedPlayers = new ArrayList<>();

    private int maxPlayers; //maximum players allowed to be in one game
    private int minPlayersRequired; // minimum players required for the queue to start

    private String queueName;


    public Queue(T t, int minPlayersRequired) {
        this.t = t;
        this.minPlayersRequired = minPlayersRequired;
        this.queueName = ((Class) t).getSimpleName();

        QueueController.getInstance().registerEvents(this);
    }

    public boolean addPlayerToQueue(Player player) {
        if (queuedPlayers.contains(player)) {
            return false;
        }

        queuedPlayers.forEach(qp -> {
            MessageUtil.sendInfo(qp, "Player joined! Currently queued players: " + ChatColor.WHITE + (getQueuedPlayers().size() + 1));
        });
        queuedPlayers.add(player);

        if (queuedPlayers.size() >= minPlayersRequired) { // There are enough players queued
            QueueController.getInstance().getScheduler().executeInSpigotCircle(() -> {
                Bukkit.getPluginManager().callEvent(new QueueReadyEvent(queuedPlayers, this));
            });
        }
        return true;
    }

    public boolean removePlayerFromQueue(Player player) {
        if (!queuedPlayers.contains(player)) {
            return false;
        }

        queuedPlayers.remove(player);
        queuedPlayers.forEach(qp -> {
            MessageUtil.sendInfo(qp, "Player left! Currently queued players: " + ChatColor.WHITE + getQueuedPlayers().size());
        });

        return true;
    }

    @EventHandler
    public void onPlayerUnloadEvent(PlayerCharacterLogoutEvent ev) {
        queuedPlayers.remove(ev.getPlayer());
    }

    @EventHandler
    public void onQueueReady(QueueReadyEvent event) {
        //remove all players that are in a ready game
        queuedPlayers.removeAll(event.getPlayers());
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

}
