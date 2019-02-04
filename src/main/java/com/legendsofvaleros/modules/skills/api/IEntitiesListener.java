package com.legendsofvaleros.modules.skills.api;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public interface IEntitiesListener {
	void run(CombatEntity ce, List<LivingEntity> entities);
}