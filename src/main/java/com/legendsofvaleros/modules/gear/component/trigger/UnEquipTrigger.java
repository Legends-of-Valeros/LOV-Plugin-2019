package com.legendsofvaleros.modules.gear.component.trigger;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;

public class UnEquipTrigger extends CombatEntityTrigger {
	public UnEquipTrigger(ItemUnEquipEvent event) {
		super(CombatEngine.getEntity(event.getPlayer()));
	}

	public UnEquipTrigger(CombatEntity ce) {
		super(ce);
	}
}