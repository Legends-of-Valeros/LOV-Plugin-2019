package com.legendsofvaleros.modules.mount;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

public class Mount {
    private String id;

    public String getId() {
        return id;
    }

    private String name;

    public String getName() {
        return name;
    }

    private EntityType type;

    /**
     * A value representing the percentage faster a mount is compared to a player.
     */
    private float speed;

    public float getSpeed() {
        return speed;
    }

    public int getSpeedPercent() {
        return (int) (speed * 100);
    }

    private int minLevel;

    public int getMinimumLevel() {
        return minLevel;
    }

    /**
     * A player's speed is 4.3 blocks per second(bps). Their in-code value is .1.
     * <p>
     * Therefore, for each 1 added to {@link #speed}, you add 4.3 bps to the horse's speed.
     * @param playerSpeed
     */
    public double getAttributeSpeed(double playerSpeed) {
        return playerSpeed * (1 + speed);
    }

    private Material icon;

    public Material getIcon() {
        return icon;
    }

    private int cost;

    public int getCost() {
        return cost;
    }

    public Mount(String id, String name, EntityType type, float speed, int minLevel, Material icon, int cost) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.speed = speed;
        this.minLevel = minLevel;
        this.icon = icon;
        this.cost = cost;
    }

    public void hopOn(Player p) {
        net.minecraft.server.v1_12_R1.Entity entityCraft = null;
        try {
            entityCraft = ((CraftWorld) p.getWorld()).createEntity(p.getLocation(), type.getEntityClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Entity entity = entityCraft.getBukkitEntity();

        if (entity instanceof LivingEntity) {
            LivingEntity entityLiving = (LivingEntity) entity;
            entityLiving.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getAttributeSpeed(p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue()));
        }

        ((CraftWorld) p.getWorld()).addEntity(entityCraft, SpawnReason.CUSTOM);

        if (entity instanceof Horse) {
            Horse horse = (Horse) entity;
            horse.setTamed(true);
            horse.setOwner(p);
            horse.setAdult();
            horse.setJumpStrength(0);
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));

            horse.setColor(Color.WHITE);
            horse.setStyle(Style.BLACK_DOTS);
        }

        entity.addPassenger(p);

        Mounts.getInstance().riders.put(p.getUniqueId(), this);
    }

    public void kickOff(UUID id, Entity vehicle) {
        if (vehicle.getType() == type)
            vehicle.remove();

        Mounts.getInstance().riders.remove(id);
    }
}