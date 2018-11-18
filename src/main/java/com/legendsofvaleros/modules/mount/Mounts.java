package com.legendsofvaleros.modules.mount;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.playermenu.options.PlayerOptionsOpenEvent;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.*;
import java.util.Map.Entry;

public class Mounts extends ListenerModule {
    private static Mounts instance;

    public static Mounts getInstance() {
        return instance;
    }

    private MountManager manager;

    public MountManager getMountManager() {
        return manager;
    }

    public HashMap<UUID, Mount> riders = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
        NPCs.registerTrait("stablemaster", TraitMount.class);
        manager = new MountManager();

    }

    @Override
    public void onUnload() {
        for (Entry<UUID, Mount> rider : riders.entrySet())
            rider.getValue().kickOff(rider.getKey(), Bukkit.getPlayer(rider.getKey()).getVehicle());

        instance = null;

    }

    @EventHandler
    public void onCharacterOptionsOpen(PlayerOptionsOpenEvent event) {
        event.addSlot(Model.stack("menu-mounts-button").setName("Mount").create(), (gui, p, event12) -> {
            gui.close(p);

            List<Mount> playerMounts = new ArrayList<>();
            ListenableFuture<Collection<Mount>> future = manager.getMounts(Characters.getPlayerCharacter(p).getUniqueCharacterId());
            future.addListener(() -> {
                try {
                    playerMounts.addAll(future.get());

                    if (playerMounts.size() == 1) {
                        // Only one? Mount that shit!
                        playerMounts.get(0).hopOn(p);
                    } else if (playerMounts.size() > 1) {
                        GUI mountgui = new GUI("Your Mounts");
                        mountgui.type(InventoryType.DISPENSER);

                        for (int i = 0; i < playerMounts.size(); i++) {
                            final Mount m = playerMounts.get(i);
                            mountgui.slot(i, new ItemBuilder(m.getIcon()).setName(m.getName())
                                    .addLore("", "Speed: " + ChatColor.GREEN + m.getSpeedPercent() + "%")
                                    .create(), (gui1, p1, event1) -> {
                                m.hopOn(p1);
                                mountgui.close(p1);
                            });
                        }

                        mountgui.open(p);
                    } else {
                        MessageUtil.sendError(p, "You don't have any mounts!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, Utilities.asyncExecutor());
        });
    }

    @EventHandler
    public void onExitVehicle(VehicleExitEvent event) {
        if (riders.containsKey(event.getExited().getUniqueId()))
            riders.get(event.getExited().getUniqueId()).kickOff(event.getExited().getUniqueId(), event.getVehicle());
    }

    @EventHandler
    public void onCharacterLogout(PlayerCharacterLogoutEvent e) {
        riders.remove(e.getPlayer().getUniqueId());
    }
}