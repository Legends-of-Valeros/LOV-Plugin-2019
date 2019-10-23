package com.legendsofvaleros.modules.mount;

import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.mount.api.IMount;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.features.playermenu.PlayerMenu;
import com.legendsofvaleros.features.playermenu.events.PlayerOptionsOpenEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@DependsOn(BankController.class)
@DependsOn(NPCsController.class)
// TODO: Create subclass for listeners?
@ModuleInfo(name = "Mounts", info = "")
public class MountsController extends MountAPI {
    private static MountsController instance;
    public static MountsController getInstance() { return instance; }

    HashMap<UUID, Mount> riders = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;
    }

    @Override
    public void onUnload() {
        super.onUnload();

        for (Entry<UUID, Mount> rider : riders.entrySet())
            rider.getValue().kickOff(rider.getKey(), Bukkit.getPlayer(rider.getKey()).getVehicle());
    }

    @EventHandler
    public void onCharacterOptionsOpen(PlayerOptionsOpenEvent event) {
        List<IMount> mounts = new ArrayList<>(getMounts(Characters.getPlayerCharacter(event.getPlayer())));

        if(mounts.size() == 0) return;

        event.addSlot(Models.stack("menu-mounts-button").setName("Mount").create(), (gui, p, event12) -> {
            gui.close(p);

            if (mounts.size() == 1) {
                // Only one? Mount that shit!
                mounts.get(0).hopOn(p);
            } else if (mounts.size() > 1) {
                GUI mountgui = new GUI("Your Mounts");
                mountgui.type(InventoryType.DISPENSER);

                for (int i = 0; i < mounts.size(); i++) {
                    final IMount m = mounts.get(i);
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