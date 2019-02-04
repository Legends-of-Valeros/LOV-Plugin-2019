package com.legendsofvaleros.modules.graveyard;

import com.codingforcookies.robert.core.GUI;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.zones.Zone;
import com.legendsofvaleros.modules.zones.ZonesController;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

public class Graveyard {
	/**
	 * Used for debugging.
	 */
	private Hologram hologram;
	private TextLine textZone, textRadius;

	public String zone;
	public Zone getZone() { return ZonesController.manager().getZone(zone); }
	
	public String worldName;
	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}

	public int x;
	public int y;
	public int z;

	private Location location;

	public Location getLocation() {
		if (location == null)
			location = new Location(getWorld(), x, y, z);
		return location;
	}

	public int radius;
	public int getRadius() {
		return radius;
	}

	public Hologram getHologram() {
		if(hologram == null) {
			hologram = HologramsAPI.createHologram(LegendsOfValeros.getInstance(), getLocation());
			textZone = hologram.appendTextLine(ChatColor.GOLD + "" + ChatColor.BOLD + getZone().name);
			textRadius = hologram.appendTextLine("Radius: " + getRadius());
			hologram.getVisibilityManager().setVisibleByDefault(LegendsOfValeros.getMode().allowEditing());

			ItemLine touchLine = hologram.appendItemLine(new ItemStack(Material.CLAY_BRICK));
			touchLine.setPickupHandler((p) -> {
				if (p.isSneaking())
					new GraveyardEditorGUI(this).open(p, GUI.Flag.NO_PARENTS);
			});

			hologram.teleport(hologram.getLocation().add(0, hologram.getHeight(), 0));
		}

		return hologram;
	}
}