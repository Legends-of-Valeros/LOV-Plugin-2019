package com.legendsofvaleros.modules.professions.gathering.mining;

import com.legendsofvaleros.modules.professions.gathering.GatheringNode;
import com.legendsofvaleros.modules.professions.gathering.GatheringType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Crystall on 03/25/2019
 */
public class MiningNode extends GatheringNode {

    public MiningNode(Location location, String zoneId, int tier) {
        super(location, zoneId, tier, GatheringType.MINING);
    }

    @Override
    public void setGlowing() {
        if (isGlowing()) {
            return;
        }
        Location slimePos = getLocation().add(.5, -.01, .5);
        Slime slime = (Slime) slimePos.getWorld().spawnEntity(slimePos, EntityType.SLIME);
        slime.setAI(false); // Prevent slimes from doing anything.
        slime.setGravity(false); // Prevent slimes from moving by gravity.
        slime.setSize(2);
        slime.setInvulnerable(true);
        slime.setCollidable(false);
        slime.setHealth(1);
        slime.teleport(slimePos); // Fix for NoAI cancelling the entity from facing the correct location.
        slime.setGlowing(true); // Set as glowing. TODO: Glow correct color.
        slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1, Integer.MAX_VALUE), true);
        glowing = slime;
    }


    @Override
    public Material getNodeMaterial() {
        return MiningTier.getTier(getTier()).getOreType();
    }

}
