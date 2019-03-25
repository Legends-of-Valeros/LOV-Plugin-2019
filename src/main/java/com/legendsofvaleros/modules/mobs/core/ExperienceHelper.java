package com.legendsofvaleros.modules.mobs.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public class ExperienceHelper {
    public static int getExperience(PlayerCharacter pc, Mob.Instance entity) {
        int xp = entity.mob.getExperience();

        // Give slightly more xp for players with levels lower than the enemy
        if (entity.level > pc.getExperience().getLevel()) {
            xp *= (1 + 0.05 * (entity.level - pc.getExperience().getLevel()));
        } else if (entity.level < pc.getExperience().getLevel()) {
            xp *= (1 - 0.15 * (pc.getExperience().getLevel() - entity.level));
        }
        return xp < 0 ? 0 : xp;
    }
}