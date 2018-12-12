package com.legendsofvaleros.modules.pvp;

import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.bank.Currency;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.dueling.Duel;
import com.legendsofvaleros.modules.dueling.Dueling;
import com.legendsofvaleros.modules.parties.Parties;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.zones.Zones;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(Bank.class)
public class PvP extends ModuleListener {
    public static final float DAMAGE_MULTIPLIER = 0.6f;

    private static PvP instance;
    public static PvP getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        Bank.registerCurrency("honor", new Currency() {
            @Override
            public String getDisplay(long amount) {
                return amount == 0 ? null : "✝ " + amount;
            }

            @Override
            public String getName() {
                return "Honor";
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(CombatEngineDamageEvent event) {
        CombatEntity attacker = event.getAttacker();
        CombatEntity target = event.getDamaged();

        if (!attacker.isPlayer() || !target.isPlayer()) return;

        /*if(!attackerToggle.isEnabled() || !targetToggle.isEnabled() || attackerToggle.getPriority() != targetToggle.getPriority()) {
            event.setCancelled(true);
            return;
        }*/

        Player p1 = (Player)event.getAttacker().getLivingEntity();
        if (!Characters.isPlayerCharacterLoaded(p1)) { event.setCancelled(true); return; }

        Player p2 = (Player)event.getDamaged().getLivingEntity();
        if (!Characters.isPlayerCharacterLoaded(p2)) { event.setCancelled(true); return; }


        // If PvP is disabled in the zone
        if(Modules.isLoaded(Zones.class)) {
            if(!Zones.manager().getZone(p1).pvp
                || !Zones.manager().getZone(p2).pvp) {
                event.setCancelled(true);
            }
        }

        if(Modules.isLoaded(Parties.class)) {
            // Disable PvP within parties
        }

        if(Modules.isLoaded(Dueling.class)) {
            Duel duel = Dueling.getInstance().getDuel(p1, p2);
            if(duel != null)
                event.setCancelled(false);
        }

        if(!event.isCancelled())
            event.newDamageModifierBuilder("PvP")
                    .setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
                    .setValue(PvP.DAMAGE_MULTIPLIER)
                .build();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPvPDeath(CombatEngineDeathEvent event) {
        CombatEntity killer = event.getKiller();
        CombatEntity target = event.getDied();

        if(killer == null || !killer.isPlayer() || !target.isPlayer()) return;

        if (!Characters.isPlayerCharacterLoaded(killer.getUniqueId())) return;

        /*if (killerToggle.isEnabled() && targetToggle.isEnabled() && killerToggle.getPriority() == targetToggle.getPriority()) {
            if (killerToggle.getHonorPoints() > 0) {
                PlayerBank bank = Bank.getBank(killerCharacter);
                bank.addCurrency("honor", killerToggle.getHonorPoints());
            }
        }*/
    }

}
