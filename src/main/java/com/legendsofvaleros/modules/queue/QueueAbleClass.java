package com.legendsofvaleros.modules.queue;

import com.legendsofvaleros.modules.arena.Team;

/**
 * Created by Crystall on 08/04/2019
 * Represents a class that is able to be
 */
public interface QueueAbleClass {

    /**
     * Called right after the queue is ready to start the "game"
     */
    void onInit();

    /**
     * Called then the game has finished
     * @param winner
     */
    void onFinish(Team winner);

    /**
     * Called whenever the game is cancelled
     */
    void onCancel();

}
