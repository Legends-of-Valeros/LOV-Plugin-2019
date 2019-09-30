package com.legendsofvaleros.modules.queue.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.core.GuiItem;
import org.bukkit.Material;

/**
 * Created by Crystall on 08/02/2019
 */
public class BattlegroundQueueGui extends GUI {

    public BattlegroundQueueGui() {
        super("Battleground");
        init();
    }

    private void init() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            slot(i, Material.BLACK_STAINED_GLASS_PANE, (gui, p, e) -> {
                //set "background" UI
            });
        }

        slot(13, GuiItem.BG_TEAM_DEATHMATCH.toItemStack(), (gui, p, e) -> {
            //TODO
        });
    }
}
