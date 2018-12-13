package com.legendsofvaleros.modules.characters.skill;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkillTargetEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

    private boolean cancelled = false;
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { cancelled = cancel; }

    final Skill skill;
    public Skill getSkill() { return skill; }

    final CombatEntity user, target;
    public CombatEntity getUser() { return user; }
    public CombatEntity getTarget() { return target; }

    final Boolean good;
    public Boolean isGood() { return good; }

    public SkillTargetEvent(Skill skill, CombatEntity user, CombatEntity target, Boolean good) {
        this.skill = skill;
        this.user = user;
        this.target = target;
        this.good = good;
    }
}