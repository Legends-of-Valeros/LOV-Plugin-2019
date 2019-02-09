package com.legendsofvaleros.modules.npcs.trait;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.Slot;
import com.codingforcookies.robert.window.ExpandingGUI;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class TraitHelper {
    public static void onLeftClick(String title, Player player, LOVTrait[] traits) {
        SettableFuture<Boolean> future = SettableFuture.create();
        List<Slot> slots = new ArrayList<>();
        future.addListener(() -> {
            if (slots.size() == 0) return;

            if (slots.size() == 1) {
                slots.get(0).action.doAction(null, player, null);
            } else {
                showSeparated(title, player, slots);
            }
        }, NPCsController.getInstance().getScheduler()::sync);


        AtomicInteger futuresLeft = new AtomicInteger(traits.length);

        for (LOVTrait trait : traits) {
            SettableFuture<Slot> traitSlot = SettableFuture.create();

            trait.onLeftClick(player, traitSlot);

            traitSlot.addListener(() -> {
                addSlotListener(player, slots, traitSlot);

                if (futuresLeft.decrementAndGet() == 0)
                    future.set(true);
            }, NPCsController.getInstance().getScheduler()::async);
        }
    }

    private static void addSlotListener(Player player, List<Slot> slots, SettableFuture<Slot> traitSlot) {
        try {
            Slot slot = traitSlot.get();
            if (slot != null)
                slots.add(slot);
        } catch (Exception e) {
            MessageUtil.sendSevereException(NPCsController.getInstance(), player, e);
        }
    }

    public static void onRightClick(String title, Player player, LOVTrait[] traits) {
        SettableFuture<List<Slot>> future = SettableFuture.create();
        future.addListener(() -> {
            try {
                List<Slot> slots = future.get();
                if (slots.size() == 0) return;

                showSeparated(title, player, slots);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, NPCsController.getInstance().getScheduler()::sync);

        List<Slot> slots = new ArrayList<>();
        AtomicInteger futuresLeft = new AtomicInteger(traits.length);

        for (LOVTrait trait : traits) {
            SettableFuture<Slot> traitSlot = SettableFuture.create();

            trait.onRightClick(player, traitSlot);

            traitSlot.addListener(() -> {
                addSlotListener(player, slots, traitSlot);

                if (futuresLeft.decrementAndGet() == 0)
                    future.set(slots);
            }, NPCsController.getInstance().getScheduler()::async);
        }
    }

    public static void showSeparated(String title, Player player, List<Slot> slots) {
        GUI gui = new ExpandingGUI(title, slots);

        if (slots.size() == 1) {
            slots.get(0).action.doAction(gui, player, null);
        } else {
            // Resync with the main thread.
            NPCsController.getInstance().getScheduler().executeInSpigotCircle(() -> gui.open(player));
        }
    }
}
