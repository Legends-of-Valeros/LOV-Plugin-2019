package com.legendsofvaleros.modules.graveyard.core;

import com.codingforcookies.robert.core.GuiFlag;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.zones.api.IZone;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Graveyard {
    /**
     * Used for debugging.
     */
    private transient Hologram hologram;
    private transient TextLine textZone, textRadius;

    private IZone zone;

    private Location location;
    public int radius;

    public Graveyard(Zone zone, Location location, int radius) {
        this.zone = zone;
        this.location = location;
        this.radius = radius;
    }

    public Location getLocation() {
        return location;
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
                    new GraveyardEditorGUI(this).open(p, GuiFlag.NO_PARENTS);
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

    public int getRadius() {
        return radius;
    }
}