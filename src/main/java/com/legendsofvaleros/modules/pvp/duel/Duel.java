package com.legendsofvaleros.modules.pvp.duel;

import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.duel.component.DuelComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Duel {

    protected byte pvpPriority;
    protected List<DuelTeam> duelTeams;
    protected Map<Class, DuelComponent> components;

    public Duel(DuelTeam... duelTeams) {
        this.duelTeams = Arrays.asList(duelTeams);
        this.components = new HashMap<>();
    }

    public byte getPriority() {
        return pvpPriority;
    }

    public List<DuelTeam> getDuelTeams() {
        return duelTeams;
    }

    public void handleDamage(CombatEngineDamageEvent event) {
        for (DuelComponent component : components.values()) {
            component.handleDamage(this, event);
        }
    }

    public void handleDeath(CombatEngineDeathEvent event) {
        for (DuelComponent component : components.values()) {
            component.handleDeath(this, event);
        }
    }

    public void addComponent(DuelComponent component) {
        components.put(component.getClass(), component);
    }

    public void removeComponent(Class<DuelComponent> clazz) {
        components.remove(clazz);
    }

    public void victoryFor(DuelTeam team) {
        for (DuelComponent component : components.values()) {
            component.handleVictory(this, team);
        }
    }

    public void complete() {
        DuelManager.removeDuel(this);
    }
}
