package com.legendsofvaleros.modules.npcs.trait.mount;

import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.features.gui.slot.ISlotAction;
import com.legendsofvaleros.features.gui.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.bank.core.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.mount.Mount;
import com.legendsofvaleros.modules.mount.MountsController;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.Collection;

public class TraitMount extends LOVTrait {
    public String[] mounts;

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        //TODO REPLACE LEGACY MATERIAL
        slot.set(new Slot(new ItemBuilder(Material.LEGACY_IRON_BARDING).setName("Purchase Mounts").create(), (gui, p, event) -> {
            gui.close(p);

            openGUI(p);
        }));
    }

    private void openGUI(Player p) {
        GUI gui = new GUI(npc.getName());
        gui.type(InventoryType.DISPENSER);

        final PlayerCharacter pc = Characters.getPlayerCharacter(p);

        Collection<Mount> playerMounts = MountsController.getInstance().getMounts(pc);
        if (playerMounts.size() == 0) return;

        for (int i = 0; i < mounts.length; i++) {
            final Mount m = MountsController.getInstance().getMount(mounts[i]);
            boolean owned = playerMounts.contains(m);
            boolean levelTooLow;

            ItemBuilder ib = new ItemBuilder(m.getIcon()).setName(m.getName());
            ib.addLore("", "Speed: " + ChatColor.GREEN + m.getSpeedPercent() + "%");

            levelTooLow = pc.getExperience().getLevel() < m.getMinimumLevel();

            ib.addLore("Level: " + (levelTooLow ? ChatColor.RED : ChatColor.GREEN) + m.getMinimumLevel());

            ib.addLore("", owned ? ChatColor.YELLOW + "Already Owned" : "Cost: " + ChatColor.GOLD + m.getCost());

            gui.slot(i, ib.create(), owned || levelTooLow ? null : (ISlotAction) (gui1, p1, event) -> {
                if (Money.sub(Characters.getPlayerCharacter(p1), m.getCost())) {
                    MountsController.getInstance().addMount(pc.getUniqueCharacterId(), m);
                    gui1.close(p1);
                }
            });
        }

        gui.open(p);
    }
}