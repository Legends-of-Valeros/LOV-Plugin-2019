package com.legendsofvaleros.modules.dueling.listener;

import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.dueling.DuelingController;
import com.legendsofvaleros.modules.dueling.core.Duel;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DuelListener implements Listener {
    private DuelingController dueling = DuelingController.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogout(PlayerCharacterLogoutEvent event) {
        Duel d = dueling.getDuel(event.getPlayer());
        if (d != null) {
            d.onDeath(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(CombatEngineDamageEvent event) {
        // We don't care about cancelled damage events.
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getDamaged().getLivingEntity() instanceof Player) ||
                !(event.getAttacker() != null && event.getAttacker().getLivingEntity() instanceof Player)) {
            return;
        }

        Duel d = dueling.getDuel((Player) event.getDamaged().getLivingEntity(), (Player) event.getAttacker().getLivingEntity());
        if (d == null) {
            return;
        }

        // Prevent death and end the duel
        if (event.getDamaged().getStats().getRegeneratingStat(RegeneratingStat.HEALTH) - event.getFinalDamage() <= 0) {
            event.setCancelled(true);

            d.onDeath((Player) event.getDamaged().getLivingEntity());
        } else {
            d.onDamage(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void isPvPAllowed(PvPCheckEvent event) {
        // A duel should override every other PvP setting. Tis a fight
        // to the death, regardless of kinship.

        Duel d = dueling.getDuel(event.getAttacker(), event.getDamaged());
        if (d == null) {
            event.setCancelled(true);
            return;
        }

        // These two players are dueling. Allow PvP.
        event.setCancelled(false);
    }
}
