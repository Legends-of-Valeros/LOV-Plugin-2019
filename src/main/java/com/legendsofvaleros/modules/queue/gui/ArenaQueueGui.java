package com.legendsofvaleros.modules.queue.gui;

import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.core.GuiItem;
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
        init();
    }


    private void init() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            slot(i, Material.BLACK_STAINED_GLASS_PANE, (gui, p, e) -> {
                //set "background" UI
            });
        }

        // casual mode
        slot(11, GuiItem.ONE_VERSUS_ONE.toItemStack(), (gui, p, e) -> QueueController.getInstance().joinQueue(OneVersusOne.class, p));
        slot(12, GuiItem.TWO_VERSUS_TWO.toItemStack(), (gui, p, e) -> QueueController.getInstance().joinQueue(TwoVersusTwo.class, p));

        // ranked mode
        slot(14, GuiItem.RANKED_ONE_VERSUS_ONE.toItemStack(), (gui, p, e) -> QueueController.getInstance().joinQueue(OneVersusOne.class, p));
        slot(15, GuiItem.RANKED_TWO_VERSUS_TWO.toItemStack(), (gui, p, e) -> QueueController.getInstance().joinQueue(TwoVersusTwo.class, p));
    }
}