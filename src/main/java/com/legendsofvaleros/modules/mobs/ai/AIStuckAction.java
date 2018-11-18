package com.legendsofvaleros.modules.mobs.ai;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;

public class AIStuckAction implements StuckAction {
	public static final AIStuckAction INSTANCE = new AIStuckAction();
	
	@Override
	public boolean run(NPC npc, Navigator nav) {
		if(nav.getEntityTarget() instanceof LivingEntity) {
			CombatEntity ce = CombatEngine.getEntity((LivingEntity)npc.getEntity());
			ce.getThreat().setThreat((LivingEntity)nav.getEntityTarget(), 0);
		}
		
		return false;
	}
}