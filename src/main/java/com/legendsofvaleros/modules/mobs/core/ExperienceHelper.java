package com.legendsofvaleros.modules.mobs.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public class ExperienceHelper {
    public static int getExperience(PlayerCharacter pc, Mob.Instance instance) {
        int xp = instance.entity.getExperience();

        // Give slightly more xp for players with levels lower than the enemy
        if (instance.level > pc.getExperience().getLevel()) {
            xp *= (1 + 0.05 * (instance.level - pc.getExperience().getLevel()));
        } else if (instance.level < pc.getExperience().getLevel()) {
            xp *= (1 - 0.15 * (pc.getExperience().getLevel() - instance.level));
        }
        return xp < 0 ? 0 : xp;
    }
}