package com.legendsofvaleros.modules.queue.gui;

import com.codingforcookies.robert.core.GUI;
import com.legendsofvaleros.modules.queue.QueueController;
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

        type(1);
        this.init();
    }

    private void init() {

    }

    @Override
    public void onClose(Player p, InventoryView view) {
    }
}
