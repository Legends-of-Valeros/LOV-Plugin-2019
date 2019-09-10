package com.legendsofvaleros.modules.mobs.core;

import com.codingforcookies.robert.core.GUI;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.api.IEntity;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnArea {
    private static final Random rand = new Random();

    /**
     * Used for debugging.
     */
    private transient Hologram hologram;
    private transient TextLine textEntityId, textLevel, textRadius, textPadding, textEntities, textInfo, textInterval;

    private int id;

    public int getId() {
        return id;
    }

    private Location location;

    public Location getLocation() {
        return location;
    }

    /**
     * The radius that defines the length of this spawn point. Entities will spawn in this
     * radius around the point.
     */
    private int radius;

    public int getRadius() {
        return radius;
    }

    /**
     * Used by AI. This defines how much further past the spawn radius that they can exist
     * around the spawn radius.
     */
    private int padding;

    public int getPadding() {
        return padding;
    }

    /**
     * The ID of the entities that spawn here.
     */
    private IEntity entity;

    public IEntity getEntity() {
        return entity;
    }

    /**
     * Tells the spawn point what level to set the spawned entities.
     * <br />
     * Example: [1, 5] defines that an entitiy spawned here should be between level 1 and 5.
     */
    private int[] level;

    public int[] getLevelRange() {
        return level;
    }

    public int getLevel() {
        int[] levels = getLevelRange();
        return levels[0] + rand.nextInt(levels[1] - levels[0] + 1);
    }

    private short count = 1;

    public int getCount() {
        return count;
    }

    public void setCount(short count) {
        this.count = count;
    }

    private int interval = 60;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    private byte chance = 100;

    public byte getChance() {
        return chance;
    }

    public void setChance(byte chance) {
        this.chance = chance;
    }

    private transient long lastInterval = 0;

    public long getLastInterval() {
        return lastInterval;
    }

    public void markInterval() {
        lastInterval = System.currentTimeMillis();
    }

    private transient int despawnedEnemies = 0;

    public int getDespawnedEnemies() {
        return despawnedEnemies;
    }

    public void repopulated() {
        despawnedEnemies = 0;
    }

    private transient List<Mob.Instance> entities;

    public List<Mob.Instance> getEntities() {
        if (entities == null)
            entities = new ArrayList<>();
        return entities;
    }

    private transient Location ground;

    public Location getGround() {
        if (ground == null) {
            Location position = getLocation();
            ground = position.clone();
            while(position.getWorld().getBlockAt(ground.getBlockX(), ground.getBlockY() - 1, ground.getBlockZ()).getType().isTransparent())
                ground.subtract(0, 1, 0);
        }
        return ground;
    }

    public SpawnArea(Location loc, IEntity entity, int radius, int padding, int[] levels) {
        this.entity = entity;
        this.radius = radius;
        this.padding = padding;
        this.level = levels;

        this.location = loc;
    }

    public void updateStats() {
        if (hologram != null) {
            textEntities.setText(getEntities().size() + " / " + despawnedEnemies);
            textInterval.setText(Instant.ofEpochMilli(System.currentTimeMillis()).toString());
        }
    }

    public void setDebugInfo(String info) {
        if (hologram != null)
            textInfo.setText(info);
    }

    public Hologram getHologram() {
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(LegendsOfValeros.getInstance(), getLocation());
            textEntityId = hologram.appendTextLine(ChatColor.GOLD + "" + ChatColor.BOLD + entity.getName());
            textLevel = hologram.appendTextLine("[" + getLevelRange()[0] + " - " + getLevelRange()[1] + "]");
            textRadius = hologram.appendTextLine("Radius: " + getRadius());
            textPadding = hologram.appendTextLine("Padding: " + getPadding());
            textEntities = hologram.appendTextLine("");
            textInfo = hologram.appendTextLine("");
            textInterval = hologram.appendTextLine("");

            updateStats();
            hologram.getVisibilityManager().setVisibleByDefault(LegendsOfValeros.getMode().allowEditing());

            ItemLine touchLine = hologram.appendItemLine(new ItemStack(Material.ENDER_EYE));
            touchLine.setPickupHandler((p) -> {
                if (p.isSneaking())
                    new SpawnEditorGUI(this).open(p, GUI.Flag.NO_PARENTS);
            });

            hologram.teleport(hologram.getLocation().add(0, hologram.getHeight(), 0));
        }

        return hologram;
    }

    public void clear() {
        despawnedEnemies = 0;

        if (entities == null || entities.size() == 0) return;
        Mob.Instance instance;

        int i = 0;
        while (i < entities.size()) {
            instance = entities.get(i);

            if (instance.ce != null && instance.ce.getThreat() != null && instance.ce.getThreat().getTarget() != null) {
                i++;
                continue;
            }

            instance.destroy();
            despawnedEnemies++;
        }

        if (entities.size() == 0) {
            entities = null;
        }
    }

    public Mob.Instance spawn(IEntity entity) {
        Location ground = getGround();

        if (radius < 0) {
            radius = 0;
            MessageUtil.sendException(MobsController.getInstance(), "Spawn '" + id + "' has a radius of < 0, setting it to 0.");
        }

        Location loc = new Location(ground.getWorld(),
                ground.getBlockX() - (radius == 0 ? 0 : rand.nextInt(radius * 2) - radius),
                ground.getBlockY(),
                ground.getBlockZ() - (radius == 0 ? 0 : rand.nextInt(radius * 2) - radius)
        );

        // Move up until loc is a transparent block
        while(!ground.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getType().isTransparent())
            loc.add(0, 1, 0);

        // Move down until the block below is solid
        while(ground.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()).getType().isTransparent())
            loc.subtract(0, 1, 0);

        Mob.Instance instance = new Mob.Instance(entity, this, getLevel());
        instance.spawn(loc);
        return instance;
    }

    public void delete() {
        entity.getSpawns().remove(this);

        clear();
        if (hologram != null) {
            hologram.delete();
            hologram = null;
        }
    }
}