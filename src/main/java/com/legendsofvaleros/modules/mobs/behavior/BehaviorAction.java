package com.legendsofvaleros.modules.mobs.behavior;

import javax.annotation.Nonnull;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public abstract class BehaviorAction  {
	public abstract @Nonnull NodeStatus onStep(CombatEntity ce, long ticks);
	public void onTerminate(CombatEntity ce) { }
}