package com.legendsofvaleros.features.gui.core;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Stack;
import java.util.UUID;

public class RobertStack implements Listener {
    private static HashMap<UUID, Stack<GUI>> parents = new HashMap<>();

    private RobertStack() {
        LegendsOfValeros.getInstance().getServer().getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());
    }

    public static GUI top(Player p) {
        Stack<GUI> guis = parents.get(p.getUniqueId());
        if (guis == null || guis.isEmpty())
            return null;
        return guis.peek();
    }

    /**
     * Drop down one element in the parent stack.
     */
    public static void open(GUI gui, Player p) {
        if (gui == null) {
            return;
        }

        Stack<GUI> guis = parents.computeIfAbsent(p.getUniqueId(), k -> new Stack<>());

        if (! guis.isEmpty()) {
            onClose(guis.peek(), p);
        }

        guis.push(gui);
        onOpen(gui, p);
    }

    /**
     * Phases down one element in the parent stack. Basically pops off the
     * top element, calls its cleanup, and does nothing with the parent GUI.
     * @return The gui that was popped off the stack, if any.
     */
    public static GUI phaseDown(Player p) {
        Stack<GUI> guis = parents.get(p.getUniqueId());
        if (guis == null) {
            return null;
        }

        GUI gui = guis.pop();
        onClose(gui, p);

        if (guis.isEmpty()) {
            parents.remove(p.getUniqueId());
        }

        return gui;
    }

    /**
     * Drop down one element in the parent stack.
     */
    public static void down(Player p) {
        Stack<GUI> guis = parents.get(p.getUniqueId());
        if (guis == null) {
            return;
        }

        phaseDown(p);

        p.closeInventory();

        // We need to pop off the next element because <code>open()</code> pushes on the opened GUI.
        open((! guis.isEmpty() ? guis.pop() : null), p);
    }

    /**
     * Clears the stack. This only calls the close function on the top inventory in the
     * stack. All the lower GUIs should have done their cleanup.
     */
    public static void clear(Player p) {
        Stack<GUI> guis = parents.get(p.getUniqueId());
        if (guis == null) {
            return;
        }

        onClose(guis.pop(), p);
        p.closeInventory();

        parents.remove(p.getUniqueId());
    }

    private static void onOpen(GUI gui, Player p) {
        if (gui != null) {
            gui.setup(p);
            gui.onOpen(p, gui.views.get(p.getUniqueId()));
        }
    }

    private static void onClose(GUI gui, Player p) {
        if (gui == null) {
            return;
        }

        gui.onClose(p, gui.views.get(p.getUniqueId()));
        gui.cleanup(p);

        // Required due to potential desync issues.
        p.updateInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogout(PlayerQuitEvent e) {
        clear(e.getPlayer());
    }
}