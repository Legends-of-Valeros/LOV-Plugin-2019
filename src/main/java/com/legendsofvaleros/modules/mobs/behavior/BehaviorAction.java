package com.legendsofvaleros.modules.mobs.behavior;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

import javax.annotation.Nonnull;

public abstract class BehaviorAction  {
	public abstract @Nonnull NodeStatus onStep(CombatEntity ce, long ticks);
	public void onTerminate(CombatEntity ce) { }
}