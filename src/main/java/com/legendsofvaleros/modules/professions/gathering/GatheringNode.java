package com.legendsofvaleros.modules.professions.gathering;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Slime;

/**
 * Created by Crystall on 06/12/2019
 */
public abstract class GatheringNode {
    private int id;
    private World world;
    private int x;
    private int y;
    private int z;
    private String zoneId;
    private int tier;
    private transient double destroyedAt;
    protected transient Slime glowing = null;
    private GatheringType type;

    public GatheringNode(Location location, String zoneId, int tier, GatheringType type) {
        this.world = location.getWorld();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.zoneId = zoneId;
        this.tier = tier;
        this.type = type;
    }

    public void removeGlowing() {
        if (isGlowing()) {
            this.glowing.remove();
            this.glowing = null;
        }
    }

    public void setGlowing() {

    }

    public boolean isGlowing() {
        return this.glowing != null;
    }

    @Override
    protected void finalize() throws Throwable {
        removeGlowing();
        super.finalize();
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

    public double getDestroyedAt() {
        return destroyedAt;
    }

    public void setDestroyedAt(double destroyedAt) {
        this.destroyedAt = destroyedAt;
    }

    public Material getNodeMaterial() {
        return null;
    }

    public GatheringType getType() {
        return type;
    }

    public void setType(GatheringType type) {
        this.type = type;
    }
}
