package com.legendsofvaleros.modules.queue.gui;

import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.core.GuiItem;
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

        this.init();
    }

    private void init() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            slot(i, Material.BLACK_STAINED_GLASS_PANE, (gui, p, e) -> {
                //set all slots to
            });
        }
        slot(11, GuiItem.ARENA_QUEUE.toItemStack(), (gui, p, e) -> new ArenaQueueGui().open(p));
        slot(13, GuiItem.BATTLEGROUND_QUEUE.toItemStack(), (gui, p, e) -> new BattlegroundQueueGui().open(p));
        slot(15, GuiItem.DUNGEONS_QUEUE.toItemStack(), (gui, p, e) -> new DungeonQueueGui().open(p));
    }

    @Override
    public void onClose(Player p, InventoryView view) {
    }
}
