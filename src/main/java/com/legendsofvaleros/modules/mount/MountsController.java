package com.legendsofvaleros.modules.mount;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.playermenu.options.PlayerOptionsOpenEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.*;
import java.util.Map.Entry;

@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@DependsOn(BankController.class)
@DependsOn(NPCsController.class)
// TODO: Create subclass for listeners?
public class MountsController extends ModuleListener {
    private static MountsController instance;
    public static MountsController getInstance() { return instance; }

    private MountManager manager;

    public MountManager getMountManager() {
        return manager;
    }

    public HashMap<UUID, Mount> riders = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        manager = new MountManager();
    }

    @Override
    public void onUnload() {
        super.onUnload();

        for (Entry<UUID, Mount> rider : riders.entrySet())
            rider.getValue().kickOff(rider.getKey(), Bukkit.getPlayer(rider.getKey()).getVehicle());
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
            }, MountsController.getInstance().getScheduler()::async);
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