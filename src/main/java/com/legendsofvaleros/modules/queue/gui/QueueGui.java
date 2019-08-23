package com.legendsofvaleros.modules.queue.gui;

import com.codingforcookies.robert.core.GUI;
import com.legendsofvaleros.modules.queue.QueueController;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryView;

/**
 * Created by Crystall on 08/04/2019
 */
public class QueueGui extends GUI implements Listener {

    public QueueGui() {
        super("Queues");

        QueueController.getInstance().registerEvents(this);

        type(6);
        this.init();
    }

    private void init() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            slot(i, Material.BLACK_STAINED_GLASS_PANE, (gui, p, e) -> {
                //set all slots to
            });
        }
        slot(23, Material.IRON_SWORD, (gui, p, e) -> new ArenaQueueGui().open(p));
        slot(25, Material.GRASS_BLOCK, (gui, p, e) -> new BattlegroundQueueGui().open(p));
        slot(27, Material.IRON_BARS, (gui, p, e) -> new DungeonQueueGui().open(p));
    }

    @Override
    public void onClose(Player p, InventoryView view) {
    }
}
