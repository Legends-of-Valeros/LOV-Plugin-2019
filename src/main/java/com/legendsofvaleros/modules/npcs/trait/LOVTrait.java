package com.legendsofvaleros.modules.npcs.trait;

import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

public abstract class LOVTrait {
	public String npc_id;
	public NPC npc;
	public TraitLOV trait;

	public void onSpawn() { }
	public void onDespawn() { }

	/**
	 * If the trait is allowed to be activated. This will hide it from the menu if there are multiple
	 * traits on one NPC.
	 */
	// public void isActive(Player player, SettableFuture<Boolean> isActive) { isActive.set(true); }

	public void onLeftClick(NPC npc, Player player, SettableFuture<Slot> slot) { slot.set(null); }
	public void onRightClick(NPC npc, Player player, SettableFuture<Slot> slot) { slot.set(null); }
}