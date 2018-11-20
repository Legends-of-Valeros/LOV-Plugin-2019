package com.legendsofvaleros.modules.mobs;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.mobs.core.Mob;

public class ExperienceHelper {
    public static int getExperience(PlayerCharacter pc, Mob.Instance entity) {
        int xp = entity.mob.getExperience();

        // Give slightly more xp for players with levels lower than the enemy
        if(entity.level > pc.getExperience().getLevel())
            xp *= (1 + 0.05 * (entity.level - pc.getExperience().getLevel()));
        else if (entity.level < pc.getExperience().getLevel())
            xp *= (1 - 0.15 * (pc.getExperience().getLevel() - entity.level));

        if(xp < 0) return 0;
        return xp;
    }
}