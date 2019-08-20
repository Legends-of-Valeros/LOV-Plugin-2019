package com.legendsofvaleros.modules.mobs.core;

import com.codingforcookies.robert.core.GuiFlag;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

    /**
     * The radius that defines the length of this spawn point. Entities will spawn in this
     * radius around the point.
     */
    private int radius;

    private int id;
    private World world;

    private int x;
    private int y;
    private int z;

    private transient Location location;

    private transient Mob mob;

    private short count = 1;
    private int interval = 60;
    private byte chance = 100;
    private transient long lastInterval = 0;
    private transient int despawnedEnemies = 0;
    private transient List<Mob.Instance> entities;
    private transient Location ground;

    /**
     * Used by AI. This defines how much further past the spawn radius that they can exist
     * around the spawn radius.
     */
    private int padding;

    /**
     * The ID of the entities that spawn here.
     */
    private String entityId;

    /**
     * Tells the spawn point what level to set the spawned entities.
     * <br />
     * Example: 1-5 defines that an entitiy spawned here should be between level 1 and 5.
     */
    private String level;
    private int[] levels;

    public SpawnArea(Location loc, String entityId, int radius, int padding, int[] levels) {
        this.world = loc.getWorld();
        this.entityId = entityId;
        this.radius = radius;
        this.padding = padding;
        this.level = levels[0] + "-" + levels[1];

        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
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
            textEntityId = hologram.appendTextLine(ChatColor.GOLD + "" + ChatColor.BOLD + entityId);
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
                    new SpawnEditorGUI(this).open(p, GuiFlag.NO_PARENTS);
            });

            hologram.teleport(hologram.getLocation().add(0, hologram.getHeight(), 0));
        }

        return hologram;
    }

    public void clear() {
        despawnedEnemies = 0;

        if (entities == null || entities.isEmpty()) {
            return;
        }
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

        if (entities.isEmpty()) {
            entities = null;
        }
    }

    public Mob.Instance spawn(Mob mob) {
        World world = getWorld();
        Location ground = getGround();

        if (radius < 0) {
            radius = 0;
            MessageUtil.sendException(MobsController.getInstance(), "Spawn '" + id + "' has a radius of < 0, setting it to 0.");
        }

        Location loc = new Location(world,
                ground.getBlockX() - (radius == 0 ? 0 : rand.nextInt(radius * 2) - radius),
                ground.getBlockY(),
                ground.getBlockZ() - (radius == 0 ? 0 : rand.nextInt(radius * 2) - radius)
        );

        // Move up until loc is a transparent block
        while (! world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getType().isTransparent())
            loc.add(0, 1, 0);

        // Move down until the block below is solid
        while (world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()).getType().isTransparent())
            loc.subtract(0, 1, 0);

        Mob.Instance instance = new Mob.Instance(mob, this, getLevel());
        instance.spawn(loc);
        return instance;
    }

    public void delete() {
        Mob mob = getMob();
        if (mob != null) {
            mob.getSpawns().remove(this);
        }

        clear();
        if (hologram != null) {
            hologram.delete();
            hologram = null;
        }
    }

    public int[] getLevelRange() {
        if (levels == null)
            levels = new int[] {
                    level != null ? Integer.parseInt(level.split("-")[0]) : 0,
                    level != null ? Integer.parseInt(level.split("-")[1]) : 0};
        return levels;
    }

    public int getLevel() {
        int[] levels = getLevelRange();
        return levels[0] + rand.nextInt(levels[1] - levels[0] + 1);
    }


    public int getCount() {
        return count;
    }

    public void setCount(short count) {
        this.count = count;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }


    public byte getChance() {
        return chance;
    }

    public void setChance(byte chance) {
        this.chance = chance;
    }


    public long getLastInterval() {
        return lastInterval;
    }

    public void markInterval() {
        lastInterval = System.currentTimeMillis();
    }

    public int getDespawnedEnemies() {
        return despawnedEnemies;
    }

    public void repopulated() {
        despawnedEnemies = 0;
    }

    public List<Mob.Instance> getEntities() {
        if (entities == null)
            entities = new ArrayList<>();
        return entities;
    }

    public Location getGround() {
        if (ground == null) {
            World world = getWorld();
            Location position = getLocation();
            ground = position.clone();
            while (world.getBlockAt(ground.getBlockX(), ground.getBlockY() - 1, ground.getBlockZ()).getType().isTransparent())
                ground.subtract(0, 1, 0);
        }
        return ground;
    }


    public int getId() {
        return id;
    }


    public World getWorld() {
        return world;
    }


    public Location getLocation() {
        if (location == null) {
            location = new Location(getWorld(), x, y, z);
        }
        return location;
    }


    public int getRadius() {
        return radius;
    }


    public int getPadding() {
        return padding;
    }


    public String getEntityId() {
        return entityId;
    }

    public Mob getMob() {
        if (mob == null)
            mob = MobsController.getInstance().getEntity(entityId);
        return mob;
    }

}