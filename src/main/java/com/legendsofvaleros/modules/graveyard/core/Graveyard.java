package com.legendsofvaleros.modules.graveyard.core;

import com.codingforcookies.robert.core.GUI;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.api.IZone;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class Graveyard {
    /**
     * Used for debugging.
     */
    private transient Hologram hologram;
    private transient TextLine textZone, textRadius;
    private transient Location location;

    private IZone zone;

    public World world;
    public int x;
    public int y;
    public int z;
    public int radius;

    public Graveyard(Zone zone, Location location, int radius) {
        this.zone = zone;
        this.world = location.getWorld();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.radius = radius;
    }

    public Hologram getHologram() {
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(LegendsOfValeros.getInstance(), getLocation());
            textZone = hologram.appendTextLine(ChatColor.GOLD + "" + ChatColor.BOLD + getZone().getName());
            textRadius = hologram.appendTextLine("Radius: " + getRadius());
            hologram.getVisibilityManager().setVisibleByDefault(LegendsOfValeros.getMode().allowEditing());

            ItemLine touchLine = hologram.appendItemLine(new ItemStack(Material.BRICK));
            touchLine.setPickupHandler((p) -> {
                if (p.isSneaking())
                    new GraveyardEditorGUI(this).open(p, GUI.Flag.NO_PARENTS);
            });

            hologram.teleport(hologram.getLocation().add(0, hologram.getHeight(), 0));
        }

        return hologram;
    }

    @Override
    public String toString() {
        return "Graveyard(id=" + zone.getId() + ", location=" + getLocation() + ")";
    }

    public IZone getZone() {
        return this.zone;
    }

    public World getWorld() {
        return world;
    }

    public int getRadius() {
        return radius;
    }

    public Location getLocation() {
        if (location == null) {
            location = new Location(world, x, y, z);
        }
        return location;
    }
}