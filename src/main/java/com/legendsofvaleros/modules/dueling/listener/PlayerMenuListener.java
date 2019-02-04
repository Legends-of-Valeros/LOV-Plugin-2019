package com.legendsofvaleros.modules.dueling.listener;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.dueling.DuelingController;
import com.legendsofvaleros.modules.playermenu.PlayerMenuOpenEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class PlayerMenuListener implements Listener {
    private DuelingController dueling = DuelingController.getInstance();

    private HashMap<Player, Player> duelRequests = new HashMap<>();

    @EventHandler
    public void onCharacterMenuOpen(PlayerMenuOpenEvent event) {
        event.addSlot(new ItemBuilder(Material.IRON_SWORD).setName("Duel").create(), (gui, p, ice) -> {
            gui.close(p);

            if(dueling.getDuel(p) != null) {
                MessageUtil.sendError(p, "You are already in a duel.");
                return;
            }

            if(dueling.getDuel(event.getClicked()) != null) {
                MessageUtil.sendError(p, "That player is already in a duel.");
                return;
            }

            if(Characters.getInstance().isInCombat(p)) {
                MessageUtil.sendError(p, "You cannot start a duel while currently in combat.");
                return;
            }

            if(Characters.getInstance().isInCombat(event.getClicked())) {
                MessageUtil.sendError(p, "You cannot start a duel with a player that is currently in combat.");
                return;
            }

            duelRequests.put(p, event.getClicked());

            if (duelRequests.containsKey(p) && duelRequests.containsValue(event.getClicked())
                    && duelRequests.containsKey(event.getClicked()) && duelRequests.containsValue(p)) {
                duelRequests.remove(p);
                duelRequests.remove(event.getClicked());

                if (p.getLocation().distance(event.getClicked().getLocation()) > 10) {
                    MessageUtil.sendError(p, "You are too far away to do that.");
                    return;
                }

                dueling.createDuel(p, event.getClicked());
                return;
            }

            MessageUtil.sendUpdate(p, "You have challenged " + event.getClicked().getName() + " to a duel.");
            MessageUtil.sendUpdate(event.getClicked(), p.getName() + " has challenged you to a duel.");
        });
    }
}
