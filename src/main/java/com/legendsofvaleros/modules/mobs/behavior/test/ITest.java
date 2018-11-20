package com.legendsofvaleros.modules.mobs.behavior.test;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

@FunctionalInterface
public interface ITest {
	boolean isSuccess(CombatEntity ce);
}