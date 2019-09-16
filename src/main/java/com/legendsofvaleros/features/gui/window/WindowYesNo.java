package com.legendsofvaleros.features.gui.window;

import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public abstract class WindowYesNo extends GUI {
    public WindowYesNo() {
        this(null);
    }

    public WindowYesNo(String title) {
        super(title == null ? "Are you sure?" : title);

        fixed();

        type(InventoryType.HOPPER);

        slot(1, new ItemBuilder(Material.SLIME_BALL).setName(ChatColor.GREEN + "Accept").create(), (gui, p, event) -> {
            gui.close(p);

            onAccept(gui, p);
        });

        slot(3, new ItemBuilder(Material.MAGMA_CREAM).setName(ChatColor.RED + "Decline").create(), (gui, p, event) -> {
            gui.close(p);

            onDecline(gui, p);
        });
    }

    public abstract void onAccept(GUI gui, Player p);

    public abstract void onDecline(GUI gui, Player p);
}