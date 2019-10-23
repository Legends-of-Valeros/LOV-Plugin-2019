package com.legendsofvaleros.modules.npcs.trait.bank.repair;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.features.gui.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.bank.gui.ItemMorphGUI;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.component.core.GearDurability;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.gear.event.RepairItemEvent;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class TraitBlacksmith extends LOVTrait {
    double cost = 1;

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        slot.set(new Slot(new ItemBuilder(Material.IRON_INGOT).setName("Repair Items").create(), (gui, p, event) -> {
            gui.close(p);

            new RepairView(cost).open(p);
        }));
    }

    private class RepairView extends ItemMorphGUI {
        private ItemStack stack;

        @Override
        public void onOpen(Player p, InventoryView view) {
            stack = p.getInventory().getItem(17);
            applyStacked(p);
        }

        @Override
        public void onClose(Player p, InventoryView view) {
            super.onClose(p, view);
            p.getInventory().setItem(17, stack);
        }

        public void applyStacked(Player p) {
            p.getInventory().setItem(17, Models.merge("menu-ui-blacksmith", stack));
        }

        double costFactor;

        public RepairView(double costFactor) {
            super("Blacksmith");

            this.costFactor = costFactor;
        }

        @Override
        public boolean isBuy() {
            return true;
        }

        @Override
        public boolean isValid(Gear.Instance item) {
            if (!item.hasComponent(GearDurability.Component.class))
                return false;
            return item.getPersist(GearDurability.Component.class).max > 0;
        }

        @Override
        public long getWorth(Gear.Instance item) {
            Integer max = item.getPersist(GearDurability.Component.class).max;
            Integer current = item.getPersist(GearDurability.Component.class).current;
            return (int) Math.ceil((max - current) * costFactor);
        }

        @Override
        public void executeMorph(PlayerCharacter pc, Gear.Instance item) {
            RepairItemEvent repairEvent = new RepairItemEvent(pc.getPlayer(), item);
            Bukkit.getPluginManager().callEvent(repairEvent);

            GearDurability.Persist persist = item.getPersist(GearDurability.Component.class);
            persist.current = persist.max;

            ItemUtil.giveItem(pc, item);
        }

        @Override
        public void onCompleted(PlayerCharacter pc) {
            applyStacked(pc.getPlayer());

            pc.getPlayer().playSound(pc.getLocation(), "ui.repair", 1F, 1F);
        }
    }
}