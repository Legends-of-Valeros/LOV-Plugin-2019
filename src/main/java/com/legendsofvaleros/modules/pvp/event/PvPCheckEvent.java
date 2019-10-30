package com.legendsofvaleros.modules.pvp.event;

import com.legendsofvaleros.modules.classes.skills.Skill;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PvPCheckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    private Player p1, p2;

    public Player getAttacker() {
        return p1;
    }

    public Player getDamaged() {
        return p2;
    }

    private Skill skill;

    public Skill getSkill() {
        return skill;
    }

    public PvPCheckEvent(Player attacker, Player damaged, Skill skill) {
        this.p1 = attacker;
        this.p2 = damaged;
        this.skill = skill;
    }
}
