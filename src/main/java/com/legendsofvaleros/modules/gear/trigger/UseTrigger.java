package com.legendsofvaleros.modules.gear.trigger;

import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import org.bukkit.event.player.PlayerInteractEvent;

public class UseTrigger extends CombatEntityTrigger {
	public UseTrigger(PlayerInteractEvent event) {
		super(CombatEngine.getEntity(event.getPlayer()));
	}
}