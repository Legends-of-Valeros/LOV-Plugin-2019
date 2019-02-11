package com.legendsofvaleros.modules.mobs.core;

import com.codingforcookies.robert.core.GUI;
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
    private Hologram hologram;
    private TextLine textEntityId, textLevel, textRadius, textPadding, textEntities, textInfo, textInterval;

    private int id;

    private World world;
    public World getWorld() {
        return world;
    }

    private int x;
    private int y;
    private int z;

    private Location location;
    public Location getLocation() {
        if (location == null)
            location = new Location(getWorld(), x, y, z);
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
    private String entityId;
    public String getEntityId() {
        return entityId;
    }

    private Mob mob;
    public Mob getMob() {
        if (mob == null)
            mob = MobsController.getInstance().getApi().getEntity(entityId);
        return mob;
    }

    /**
     * Tells the spawn point what level to set the spawned entities.
     * <br />
     * Example: 1-5 defines that an entitiy spawned here should be between level 1 and 5.
     */
    private String entityLevel;
    private int[] levels;

    public int[] getLevelRange() {
        if (levels == null)
            levels = new int[]{
                    Integer.parseInt(entityLevel.split("-")[0]),
                    Integer.parseInt(entityLevel.split("-")[1])};
        return levels;
    }

    public int getLevel() {
        int[] levels = getLevelRange();
        return levels[0] + rand.nextInt(levels[1] - levels[0] + 1);
    }

    public short spawnCount = 1;
    public int getSpawnCount() {
        return spawnCount;
    }

    public int spawnInterval = 60;
    public int getSpawnInterval() {
        return spawnInterval;
    }

    public byte spawnChance = 100;
    public byte getSpawnChance() {
        return spawnChance;
    }

    private long lastInterval = 0;
    public long getLastSpawn() {
        return lastInterval;
    }
    public void markInterval() {
        lastInterval = System.currentTimeMillis();
    }

    private int despawnedEnemies = 0;
    public int getDespawnedEnemies() {
        return despawnedEnemies;
    }
    public void repopulated() {
        despawnedEnemies = 0;
    }

    private List<Mob.Instance> entities;
    public List<Mob.Instance> getEntities() {
        if (entities == null)
            entities = new ArrayList<>();
        return entities;
    }

    private Location ground;
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

    public SpawnArea(Location loc, String entityId, int radius, int padding, int[] levels) {
        this.world = loc.getWorld();
        this.entityId = entityId;
        this.radius = radius;
        this.padding = padding;
        this.entityLevel = levels[0] + "-" + levels[1];

        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.location = loc;
    }

    public void updateStats() {
        if(hologram != null) {
            textEntities.setText(getEntities().size() + " / " + despawnedEnemies);
            textInterval.setText(Instant.ofEpochMilli(System.currentTimeMillis()).toString());
        }
    }

    public void setDebugInfo(String info) {
        if(hologram != null)
            textInfo.setText(info);
    }

    public Hologram getHologram() {
        if(hologram == null) {
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

            ItemLine touchLine = hologram.appendItemLine(new ItemStack(Material.EYE_OF_ENDER));
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

        if (entities.size() == 0)
            entities = null;
    }

    public Mob.Instance spawn(Mob mob) {
        World world = getWorld();
        Location ground = getGround();

        if(radius < 0) {
            radius = 0;

            MessageUtil.sendException(MobsController.getInstance(), "Spawn '" + id + "' has a radius of < 0, setting it to 0.");
        }

        Location loc = new Location(world,
                ground.getBlockX() - (radius == 0 ? 0 : rand.nextInt(radius * 2) - radius),
                ground.getBlockY(),
                ground.getBlockZ() - (radius == 0 ? 0 : rand.nextInt(radius * 2) - radius)
        );

        // Move up until loc is a transparent block
        while (!world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getType().isTransparent())
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
        if (mob != null)
            mob.getSpawns().remove(this);

        clear();

        if (hologram != null) {
            hologram.delete();
            hologram = null;
        }
    }
}