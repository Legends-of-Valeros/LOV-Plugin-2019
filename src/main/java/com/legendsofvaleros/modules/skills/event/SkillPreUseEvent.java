package com.legendsofvaleros.modules.skills.event;

import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkillPreUseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final LivingEntity entity;
    private final CombatEntity combatEntity;

    private final Skill skill;

    public Skill getSkill() {
        return skill;
    }

    private final int level;

    public int getLevel() {
        return level;
    }

    private boolean cancelled = false;

    public SkillPreUseEvent(CombatEntity caster, Skill skill, int level) {
        this.entity = caster.getLivingEntity();
        this.combatEntity = caster;
        this.skill = skill;
        this.level = level;
    }

    /**
     * Gets the instance that the combat data was created for.
     * @return The instance this event is for.
     */
    public LivingEntity getLivingEntity() {
        return entity;
    }

    /**
     * Gets the combat data object that was created for the instance.
     * @return The instance's combat data.
     */
    public CombatEntity getCombatEntity() {
        return combatEntity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}