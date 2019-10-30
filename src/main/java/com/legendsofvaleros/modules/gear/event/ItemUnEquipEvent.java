package com.legendsofvaleros.modules.gear.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.gear.core.Gear;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public class ItemUnEquipEvent extends PlayerCharacterEvent {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private Gear.Instance gearInstance;

    public Gear.Instance getGear() {
        return gearInstance;
    }

    private EquipmentSlot slot;

    public EquipmentSlot getSlot() {
        return slot;
    }

    public ItemUnEquipEvent(PlayerCharacter pc, Gear.Instance gearInstance, EquipmentSlot slot) {
        super(pc);
        this.gearInstance = gearInstance;
        this.slot = slot;
    }
}