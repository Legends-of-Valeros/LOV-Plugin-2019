package com.legendsofvaleros.modules.skills.event;

import org.bukkit.entity.LivingEntity;

@FunctionalInterface
public interface OnTouchGroundListener {
	void run(LivingEntity e);
}