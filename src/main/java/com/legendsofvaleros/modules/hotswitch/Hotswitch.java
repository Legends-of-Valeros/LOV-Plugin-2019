package com.legendsofvaleros.modules.hotswitch;

import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.hotswitch.event.PlayerSwitchHotbarEvent;
import com.legendsofvaleros.modules.hotswitch.event.PlayerUseHotbarEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
// TODO: Create subclass for listeners?
@ModuleInfo(name = "Hotswitch", info = "")
public class Hotswitch extends ModuleListener {
    private static Hotswitch instance;
    public static Hotswitch getInstance() { return instance; }

    public static final int SWITCHER_SLOT = 5;
    public static final int HELD_SLOT = 6;

    public HashMap<UUID, ItemStack> heldItems;
    private HashMap<UUID, Integer> currentHotbar;

    public int getCurrentHotbar(UUID uuid) {
        if (!currentHotbar.containsKey(uuid))
            return 0;
        return currentHotbar.get(uuid);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        heldItems = new HashMap<>();
        currentHotbar = new HashMap<>();
    }

    @EventHandler
    public void onFinishedLoading(PlayerCharacterFinishLoadingEvent e) {
        e.getPlayer().getInventory().setHeldItemSlot(HELD_SLOT);

        currentHotbar.put(e.getPlayer().getUniqueId(), -1);
        fireHotswitch(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogoutEvent(PlayerCharacterLogoutEvent e) {
        for (int i = 0; i <= SWITCHER_SLOT; i++)
            e.getPlayer().getInventory().setItem(i, null);

        heldItems.remove(e.getPlayer().getUniqueId());
        currentHotbar.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!Characters.isPlayerCharacterLoaded((Player) e.getWhoClicked())) return;

        if (e.getClickedInventory() == null)
            return;

        if (e.getClickedInventory() != e.getWhoClicked().getInventory())
            return;

        if (e.getHotbarButton() >= 0 && e.getHotbarButton() <= SWITCHER_SLOT) {
            if (e.getHotbarButton() == SWITCHER_SLOT)
                fireHotswitch((Player) e.getWhoClicked());

            e.setCancelled(true);
        } else if (e.getSlot() == SWITCHER_SLOT)
            fireHotswitch((Player) e.getWhoClicked());

        if (e.getSlot() < HELD_SLOT)
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerItemHoldEvent(PlayerItemHeldEvent e) {
        if (!Characters.isPlayerCharacterLoaded(e.getPlayer())) return;

        if (e.getPreviousSlot() == e.getNewSlot()) {
            ItemStack previous = heldItems.get(e.getPlayer().getUniqueId());
            if (previous != null) {
                ItemStack current = e.getPlayer().getInventory().getItemInMainHand();
                if (previous.isSimilar(current)) {
                    return;
                }
            }
        }

        PlayerUseHotbarEvent event = new PlayerUseHotbarEvent(e.getPlayer(), currentHotbar.get(e.getPlayer().getUniqueId()), e.getNewSlot(), e.getPlayer().getInventory().getItemInMainHand());
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (e.getNewSlot() >= HELD_SLOT)
            heldItems.put(e.getPlayer().getUniqueId(), event.getItemStack());
        else {
            if (e.getNewSlot() == SWITCHER_SLOT)
                fireHotswitch(e.getPlayer());

            e.getPlayer().getInventory().setHeldItemSlot(HELD_SLOT);
        }
    }

    private void fireHotswitch(Player p) {
        if (!Characters.isPlayerCharacterLoaded(p)) return;

        PlayerSwitchHotbarEvent event = new PlayerSwitchHotbarEvent(p, currentHotbar.get(p.getUniqueId()));
        Bukkit.getServer().getPluginManager().callEvent(event);

        currentHotbar.put(p.getUniqueId(), event.getCurrentHotbar());
    }
}