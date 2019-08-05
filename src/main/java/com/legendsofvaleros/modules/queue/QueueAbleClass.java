package com.legendsofvaleros.modules.queue;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Crystall on 08/04/2019
 * Represents a class that is able to be
 */
public interface QueueAbleClass {

    /**
     * Called then the game has finished
     */
    void onFinish();

    /**
     * Called whenever the game is cancelled
     */
    void onCancel();

    /**
     * Called when a queue has enough players and is transferring the players into the game
     * @param players
     */
    void init(List<Player> players);

}
