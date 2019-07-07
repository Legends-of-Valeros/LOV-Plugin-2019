package com.legendsofvaleros.modules.combatengine.damage.physical;

import org.bukkit.Sound;

/**
 * Different possible types of physical damage.
 */
public enum PhysicalType {

	OTHER(Sound.BLOCK_NOTE_BLOCK_BASS),
	MELEE(Sound.BLOCK_NOTE_BLOCK_BASS),
	BOW(Sound.BLOCK_NOTE_BLOCK_BASS),
	MISC_PROJECTILE(Sound.BLOCK_NOTE_BLOCK_BASS);

	private Sound hitSound;

	PhysicalType(Sound hitSound) {
		this.hitSound = hitSound;
	}

	public Sound getHitSound() {
		return hitSound;
	}
}
