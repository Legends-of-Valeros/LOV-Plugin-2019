package com.legendsofvaleros.modules.queue;

import org.bukkit.entity.Player;

/**
 * Created by Crystall on 08/04/2019
 * Represents a class that is able to be
 */
public interface QueueAbleClass {

    /**
     * Called then the game has finished
     */
    void onFinish(Player winner);

    /**
     * Called whenever the game is cancelled
     */
    void onCancel();

}
