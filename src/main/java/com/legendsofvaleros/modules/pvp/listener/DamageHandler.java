package com.legendsofvaleros.modules.pvp.listener;

import com.legendsofvaleros.modules.bank.PlayerBank;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.bank.PlayerBank;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggle;
import com.legendsofvaleros.modules.bank.PlayerBank;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DamageHandler implements Listener {

    private PvP pvp;

    public DamageHandler() {
        pvp = PvP.getInstance();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamagePlayer(CombatEngineDamageEvent event) {
        CombatEntity attacker = event.getAttacker();
        CombatEntity target = event.getDamaged();

        if (!attacker.isPlayer() || !target.isPlayer()) return;

        PvPToggle attackerToggle = pvp.getToggles().getRulingToggleFor(attacker.getUniqueId());
        PvPToggle targetToggle = pvp.getToggles().getRulingToggleFor(target.getUniqueId());

        if(!attackerToggle.isEnabled() || !targetToggle.isEnabled() || attackerToggle.getPriority() != targetToggle.getPriority()) {
            event.setCancelled(true);
            return;
        }

        event.setRawDamage(event.getRawDamage() * PvP.DAMAGE_MULTIPLIER);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPvPDeath(CombatEngineDeathEvent event) {
        CombatEntity killer = event.getKiller();
        CombatEntity target = event.getDied();

        if(killer == null || !killer.isPlayer() || !target.isPlayer()) return;

        if (!Characters.isPlayerCharacterLoaded(killer.getUniqueId())) return;

        PlayerCharacter killerCharacter = Characters.getPlayerCharacter(killer.getUniqueId());
        PvPToggle killerToggle = pvp.getToggles().getRulingToggleFor(killer.getUniqueId());
        PvPToggle targetToggle = pvp.getToggles().getRulingToggleFor(target.getUniqueId());

        if (killerToggle.isEnabled() && targetToggle.isEnabled() && killerToggle.getPriority() == targetToggle.getPriority()) {
            if (killerToggle.getHonorPoints() > 0) {
                PlayerBank bank = Bank.getBank(killerCharacter);
                bank.addCurrency("honor", killerToggle.getHonorPoints());
            }
        }
    }

}
