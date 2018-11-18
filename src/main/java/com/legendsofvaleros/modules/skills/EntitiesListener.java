package com.legendsofvaleros.modules.skills;

import java.util.List;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.entity.LivingEntity;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public interface EntitiesListener {
	void run(CombatEntity ce, List<LivingEntity> entities);
}