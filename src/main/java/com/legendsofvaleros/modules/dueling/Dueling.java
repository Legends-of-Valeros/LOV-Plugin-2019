package com.legendsofvaleros.modules.dueling;

import com.codingforcookies.robert.item.ItemBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.playermenu.PlayerMenuOpenEvent;
import com.legendsofvaleros.modules.pvp.PvPCheckEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class Dueling extends ModuleListener implements Listener {
    private static Dueling instance;
    public static Dueling getInstance() { return instance; }

    public static HashMap<Player, Player> duelRequests = new HashMap<>();

    public Table<Player, Player, Duel> duels = HashBasedTable.create();

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;
    }

    @Override
    public void onUnload() {
        super.onUnload();

        for (Cell<Player, Player, Duel> c : duels.cellSet())
            c.getValue().cancel();
    }

    public Duel getDuel(Player p1, Player p2) {
        Duel duel = null;

        if (duels.contains(p1, p2))
            duel = duels.get(p1, p2);

        else if (duels.contains(p2, p1))
            duel = duels.get(p2, p1);

        return duel;
    }

    public Duel getDuel(Player p) {
        Duel duel = null;

        if (duels.row(p).size() != 0)
            duel = duels.row(p).values().iterator().next();

        else if (duels.column(p).size() != 0)
            duel = duels.column(p).values().iterator().next();

        return duel;
    }

    @EventHandler
    public void onCharacterMenuOpen(PlayerMenuOpenEvent event) {
        event.addSlot(new ItemBuilder(Material.IRON_SWORD).setName("Duel").create(), (gui, p, ice) -> {
            gui.close(p);

            if (duels.containsRow(p) || duels.containsColumn(p)) {
                MessageUtil.sendError(p, "You are already in a duel.");
                return;
            }

            if (duels.containsRow(event.getClicked()) || duels.containsColumn(event.getClicked())) {
                MessageUtil.sendError(p, "That player is already in a duel.");
                return;
            }

            if (Characters.getInstance().isInCombat(p)) {
                MessageUtil.sendError(p, "You cannot start a duel while currently in combat.");
                return;
            }

            if (Characters.getInstance().isInCombat(event.getClicked())) {
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

                Title title = new Title("", "Ready.... Fight!", 10, 40, 10);
                title.setTimingsToTicks();
                title.setSubtitleColor(ChatColor.GOLD);
                TitleUtil.queueTitle(title, p);
                TitleUtil.queueTitle(title, event.getClicked());

                duels.put(p, event.getClicked(), new Duel(p, event.getClicked()));
                return;
            }

            MessageUtil.sendUpdate(p, "You have challenged " + event.getClicked().getName() + " to a duel.");
            MessageUtil.sendUpdate(event.getClicked(), p.getName() + " has challenged you to a duel.");
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogout(PlayerCharacterLogoutEvent event) {
        Duel d = getDuel(event.getPlayer());
        if (d != null)
            d.onDeath(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void isPvPAllowed(PvPCheckEvent event) {
        // A duel should override every other PvP setting. Tis a fight
        // to the death, regardless of kinship.

        Duel d = getDuel(event.getAttacker(), event.getDamaged());
        if(d == null) {
            // If either player is in a duel, cancel damage.
            if(getDuel(event.getAttacker()) != null || getDuel(event.getDamaged()) != null)
                event.setCancelled(true);
            return;
        }

        // These two players are dueling. Allow PvP.
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(CombatEngineDamageEvent event) {
        // We don't care about cancelled damage events.
        if(event.isCancelled()) return;

        if (!(event.getDamaged().getLivingEntity() instanceof Player) || !(event.getAttacker() != null && event.getAttacker().getLivingEntity() instanceof Player))
            return;

        Duel d = getDuel((Player)event.getDamaged().getLivingEntity(), (Player) event.getAttacker().getLivingEntity());
        if(d == null) return;

        // Prevent death and end the duel
        if (event.getDamaged().getStats().getRegeneratingStat(RegeneratingStat.HEALTH) - event.getFinalDamage() <= 0) {
            event.setCancelled(true);

            d.onDeath((Player) event.getDamaged().getLivingEntity());
        } else {
            d.onDamage(event);
        }
    }
}