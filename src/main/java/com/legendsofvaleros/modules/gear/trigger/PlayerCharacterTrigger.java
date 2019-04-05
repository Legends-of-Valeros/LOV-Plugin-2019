package com.legendsofvaleros.modules.gear.trigger;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.combatengine.CombatEngine;

public abstract class PlayerCharacterTrigger extends CombatEntityTrigger {
	public final PlayerCharacter pc;
	public PlayerCharacter getPlayerCharacter() { return pc; }
	
	public PlayerCharacterTrigger(PlayerCharacter pc) {
		super(CombatEngine.getEntity(pc.getPlayer()));
		this.pc = pc;
	}
}