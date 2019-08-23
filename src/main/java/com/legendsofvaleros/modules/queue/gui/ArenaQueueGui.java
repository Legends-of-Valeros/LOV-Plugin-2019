package com.legendsofvaleros.modules.queue.gui;

import com.codingforcookies.robert.core.GUI;
import com.legendsofvaleros.modules.arena.arenamodes.OneVersusOne;
import com.legendsofvaleros.modules.arena.arenamodes.TwoVersusTwo;
import com.legendsofvaleros.modules.queue.QueueController;
import org.bukkit.Material;

/**
 * Created by Crystall on 08/02/2019
 */
public class ArenaQueueGui extends GUI {

    public ArenaQueueGui() {
        super("Arena");
        type(1);
        init();
    }


    private void init() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            slot(i, Material.BLACK_STAINED_GLASS_PANE, (gui, p, e) -> {
                //set "background" UI
            });
        }

        // casual mode
        slot(23, Material.APPLE, (gui, p, e) -> QueueController.getInstance().joinQueue(OneVersusOne.class, p));
        slot(25, Material.APPLE, (gui, p, e) -> QueueController.getInstance().joinQueue(TwoVersusTwo.class, p));

        // ranked mode
        slot(32, Material.APPLE, (gui, p, e) -> QueueController.getInstance().joinQueue(OneVersusOne.class, p));
        slot(33, Material.APPLE, (gui, p, e) -> QueueController.getInstance().joinQueue(TwoVersusTwo.class, p));
    }
}