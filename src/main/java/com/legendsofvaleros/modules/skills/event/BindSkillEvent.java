package com.legendsofvaleros.modules.skills.event;

import com.legendsofvaleros.modules.characters.skill.Skill;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class BindSkillEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final int hotbar;

    public int getHotbar() {
        return hotbar;
    }

    private final int slot;

    public int getSlot() {
        return slot;
    }

    private final Skill skill;

    public String getSkillId() {
        return skill.getId();
    }

    public Skill getSkill() {
        return skill;
    }

    public BindSkillEvent(Player who, int hotbar, int slot, Skill skill) {
        super(who);

        this.hotbar = hotbar;
        this.slot = slot;
        this.skill = skill;
    }
}