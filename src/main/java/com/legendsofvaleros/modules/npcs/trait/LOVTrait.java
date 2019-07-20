package com.legendsofvaleros.modules.npcs.trait;

import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

public abstract class LOVTrait {
    public transient TraitLOV trait;

    public transient String id;

    public transient String npc_id;
    public transient NPC npc;

    public void onSpawn() {
    }

    public void onDespawn() {
    }

    /**
     * If the trait is allowed to be activated. This will hide it from the menu if there are multiple
     * traits on one NPC.
     */
    // public void isActive(Player player, SettableFuture<Boolean> isActive) { isActive.set(true); }
    public void onLeftClick(Player player, SettableFuture<Slot> slot) {
        slot.set(null);
    }

    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        slot.set(null);
    }
}