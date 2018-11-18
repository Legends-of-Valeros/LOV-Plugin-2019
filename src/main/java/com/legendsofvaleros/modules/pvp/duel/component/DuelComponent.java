package com.legendsofvaleros.modules.pvp.duel.component;

import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.duel.Duel;
import com.legendsofvaleros.modules.pvp.duel.DuelTeam;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;

public abstract class DuelComponent {
    public void handleDamage(Duel duel, CombatEngineDamageEvent event) {}

    public void handleDeath(Duel duel, CombatEngineDeathEvent event) {}

    public void handleVictory(Duel duel, DuelTeam victors) {}
}
