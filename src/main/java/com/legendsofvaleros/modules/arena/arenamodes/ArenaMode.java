package com.legendsofvaleros.modules.arena.arenamodes;

import com.legendsofvaleros.modules.arena.Team;
import com.legendsofvaleros.modules.queue.QueueAbleClass;

/**
 * Created by Crystall on 08/21/2019
 */
public interface ArenaMode extends QueueAbleClass {

    @Override
    default void onInit() {
    }

    default void onFinish(Team winner) {
    }

    default void onCancel() {

    }

}
