package com.legendsofvaleros.modules.skills;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public interface EntitiesListener {
	void run(CombatEntity ce, List<LivingEntity> entities);
}