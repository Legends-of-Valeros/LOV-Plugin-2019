package com.legendsofvaleros.modules.playermenu;

import com.codingforcookies.robert.window.ExpandingGUI;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.playermenu.events.PlayerMenuOpenEvent;
import com.legendsofvaleros.util.model.Model;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

// TODO: Create subclass for listeners?
@ModuleInfo(name = "PlayerMenu", info = "")
public class PlayerMenu extends ListenerModule {
    @Override
    public void onLoad() {
        super.onLoad();

        registerEvents(new InventoryManager());
    }

    public static void buildMenu(Player source, Player target) {
        PlayerMenuOpenEvent event = new PlayerMenuOpenEvent(source, target);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        ExpandingGUI gui = new ExpandingGUI(target.getName(), event.getSlots()) {
            private ItemStack stack;

            @Override
            public void onOpen(Player p, InventoryView view) {
                p.getInventory().setItem(17, Model.merge(event.getSlots().size() <= 5 ? "menu-ui-hopper" : "menu-ui-dispenser", (stack = p.getInventory().getItem(17))));
            }

            @Override public void onClose(Player p, InventoryView view) {
                p.getInventory().setItem(17, stack);
            }
        };

        gui.open(source);
    }

    @EventHandler
    public void onPlayerClickAnother(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().isSneaking()) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        if (event.getRightClicked().getType() == EntityType.PLAYER && Bukkit.getPlayer(event.getRightClicked().getName()) != null)
            buildMenu(event.getPlayer(), (Player) event.getRightClicked());
    }
}