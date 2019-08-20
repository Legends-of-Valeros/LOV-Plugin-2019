package com.legendsofvaleros.modules.playermenu.options;

import com.codingforcookies.robert.core.GuiFlag;
import com.codingforcookies.robert.window.ExpandingGUI;
import com.legendsofvaleros.modules.playermenu.events.PlayerOptionsOpenEvent;
import com.legendsofvaleros.util.model.Model;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class PlayerOptions {
    public static void open(Player p) {
        PlayerOptionsOpenEvent event = new PlayerOptionsOpenEvent(p);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        //event.addSlot(Model.stack("menu-settings-button").setName("Settings").create(), (gui, _p, _event) -> PlayerSettings.open(p));

        ExpandingGUI gui = new ExpandingGUI(p.getName(), event.getSlots()) {
            private ItemStack stack;

            @Override
            public void onOpen(Player p, InventoryView view) {
                p.getInventory().setItem(17, Model.merge(event.getSlots().size() <= 5 ? "menu-ui-hopper" : "menu-ui-3x3", (stack = p.getInventory().getItem(17))));
            }

            @Override
            public void onClose(Player p, InventoryView view) {
                p.getInventory().setItem(17, stack);
            }
        };
        gui.open(p, GuiFlag.NO_PARENTS);
    }
}