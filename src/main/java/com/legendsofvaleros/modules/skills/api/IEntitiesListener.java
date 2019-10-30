package com.legendsofvaleros.modules.skills.api;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import java.util.List;
import org.bukkit.entity.LivingEntity;

public interface IEntitiesListener {

  void run(CombatEntity ce, List<LivingEntity> entities);
}