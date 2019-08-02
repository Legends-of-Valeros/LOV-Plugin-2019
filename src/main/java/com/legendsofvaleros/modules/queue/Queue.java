package com.legendsofvaleros.modules.queue;

import org.bukkit.entity.Player;

import java.util.ArrayList;

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
    private ArrayList<T> existingGame = new ArrayList<>();

    private int maxPlayers; //maximum players allowed to be in one game
    private int minPlayersRequired; // minimum players required for the queue to start

    private String queueName;


    public Queue(T t, ArrayList<Player> queuedPlayers, ArrayList<T> existingGame, int maxPlayers,
                 int minPlayersRequired, String queueName) {
        this.t = t;
        this.queuedPlayers = queuedPlayers;
        this.existingGame = existingGame;
        this.maxPlayers = maxPlayers;
        this.minPlayersRequired = minPlayersRequired;
        this.queueName = queueName;
    }

    public ArrayList<Player> getQueuedPlayers() {
        return queuedPlayers;
    }

    public ArrayList<T> getExistingGame() {
        return existingGame;
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
