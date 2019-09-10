package com.legendsofvaleros.modules.mobs.core;

import org.bukkit.ChatColor;

public enum EntityRarity {
	NORMAL((instance) -> instance.entity.getName() + ChatColor.GOLD + ChatColor.BOLD + " Lv." + instance.level),
	RARE((instance) -> ChatColor.GRAY + "[" + instance.entity.getName() + "]" + ChatColor.GOLD + ChatColor.BOLD + " Lv." + instance.level),
	ELITE((instance) -> ChatColor.RED + "[" + instance.entity.getName() + "]" + ChatColor.GOLD + ChatColor.BOLD + " Lv." + instance.level),
	BOSS((instance) -> ChatColor.YELLOW + "[" + ChatColor.RED + ChatColor.BOLD + instance.entity.getName() + ChatColor.YELLOW + "]" + ChatColor.GOLD + ChatColor.BOLD + " Lv." + instance.level);

	final INameplateGen gen;
	EntityRarity(INameplateGen gen) {
		this.gen = gen;
	}
	
	public String newNameplate(Mob.Instance instance) {
		return gen.generate(instance);
	}
	
	@FunctionalInterface
	private interface INameplateGen {
		String generate(Mob.Instance instance);
	}
}