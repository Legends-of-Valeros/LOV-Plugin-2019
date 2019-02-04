package com.legendsofvaleros.modules.npcs.trait.mount;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.ISlotAction;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.mount.Mount;
import com.legendsofvaleros.modules.mount.Mounts;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class TraitMount extends LOVTrait {
    public String[] mounts;

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        slot.set(new Slot(new ItemBuilder(Material.IRON_BARDING).setName("Purchase Mounts").create(), (gui, p, event) -> {
            gui.close(p);

            openGUI(p);
        }));
    }

    private void openGUI(Player p) {
        GUI gui = new GUI(npc.getName());
        gui.type(InventoryType.DISPENSER);

        final PlayerCharacter pc = Characters.getPlayerCharacter(p);
        ListenableFuture<Collection<Mount>> future = Mounts.getInstance().getMountManager().getMounts(pc.getUniqueCharacterId());
        future.addListener(() -> {
            try {
                Collection<Mount> playerMounts = future.get();
                if (playerMounts.size() == 0) return;

                for (int i = 0; i < mounts.length; i++) {
                    final Mount m = Mounts.getInstance().getMountManager().getMount(mounts[i]);
                    boolean owned = playerMounts.contains(m);
                    boolean levelTooLow;

                    ItemBuilder ib = new ItemBuilder(m.getIcon()).setName(m.getName());
                    ib.addLore("", "Speed: " + ChatColor.GREEN + m.getSpeedPercent() + "%");

                    levelTooLow = pc.getExperience().getLevel() < m.getMinimumLevel();

                    ib.addLore("Level: " + (levelTooLow ? ChatColor.RED : ChatColor.GREEN) + m.getMinimumLevel());

                    ib.addLore("", owned ? ChatColor.YELLOW + "Already Owned" : "Cost: " + ChatColor.GOLD + m.getCost());

                    gui.slot(i, ib.create(), owned || levelTooLow ? null : (ISlotAction) (gui1, p1, event) -> {
                        if (Money.sub(Characters.getPlayerCharacter(p1), m.getCost())) {
                            Mounts.getInstance().getMountManager().addMount(pc.getUniqueCharacterId(), m);
                            gui1.close(p1);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            new BukkitRunnable() {
                public void run() {
                    gui.open(p);
                }
            }.runTask(LegendsOfValeros.getInstance());
        }, Mounts.getInstance().getScheduler()::async);
    }
}