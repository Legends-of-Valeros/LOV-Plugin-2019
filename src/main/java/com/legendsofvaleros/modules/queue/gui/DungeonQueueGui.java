package com.legendsofvaleros.modules.queue.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.core.GuiItem;
import org.bukkit.Material;

/**
 * Created by Crystall on 08/02/2019
 */
public class DungeonQueueGui extends GUI {

    public DungeonQueueGui() {
        super("Dungeon");
        init();
    }

    private void init() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            slot(i, Material.BLACK_STAINED_GLASS_PANE, (gui, p, e) -> {
                //set "background" UI
            });
        }

        // casual mode
        slot(13, GuiItem.DUNGEONS_QUEUE.toItemStack(), (gui, p, e) -> {
            //TODO add dungeon GUI
        });
    }
}
