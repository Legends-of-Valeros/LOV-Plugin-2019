package com.legendsofvaleros.modules.arena.arenamodes;

import com.legendsofvaleros.modules.queue.QueueAbleClass;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 08/21/2019
 */
public interface ArenaMode extends QueueAbleClass {

    default void onFinish(Player winner) {
    }

    default void onCancel() {

    }

}
