package com.legendsofvaleros.modules.professions.gathering.mining;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Crystall on 03/25/2019
 */
public class MiningNode {
    private int id;
    private World world;
    private int x;
    private int y;
    private int z;
    private String zoneId;
    private int tier;
    private transient Slime glowing = null;
    private transient double destroyedAt;

    public MiningNode(Location location, String zoneId, int tier) {
        this.world = location.getWorld();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.zoneId = zoneId;
        this.tier = tier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getLocation() {
        return new Location(world, x, y, z);
    }

    public void setLocation(Location location) {
        this.world = location.getWorld();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

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

    public void removeGlowing() {
        if (isGlowing()) {
            this.glowing.remove();
            this.glowing = null;
        }
    }

    public boolean isGlowing() {
        return this.glowing != null;
    }

    @Override
    protected void finalize() throws Throwable {
        removeGlowing();
        super.finalize();
    }

    public double getDestroyedAt() {
        return destroyedAt;
    }

    public void setDestroyedAt(double destroyedAt) {
        this.destroyedAt = destroyedAt;
    }
}
