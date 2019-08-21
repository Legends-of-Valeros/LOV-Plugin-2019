package com.legendsofvaleros.modules.graveyard;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.graveyard.commands.GraveyardCommands;
import com.legendsofvaleros.modules.graveyard.core.Graveyard;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Collection;
import java.util.List;

// Currently graveyards search the entire zone for the nearest. Should we make it search for nearest graveyards in the Section, first?
@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(GearController.class)
@DependsOn(ZonesController.class)
@ModuleInfo(name = "Graveyards", info = "")
public class GraveyardController extends GraveyardAPI {
    private static GraveyardController instance;

    public static GraveyardController getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        LegendsOfValeros.getInstance().getCommand("suicide").setExecutor((sender, arg1, arg2, arg3) -> {
            if (CombatEngine.getEntity((Player) sender) == null) {
                return false;
            }

            CombatEngine.getInstance().causeTrueDamage((Player) sender, null, CombatEngine.getEntity((Player) sender).getStats().getStat(Stat.MAX_HEALTH), ((Player) sender).getLocation());
            return true;
        });

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new GraveyardCommands());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerRespawnEvent event) {
        Graveyard data = getNearestGraveyard(ZonesController.getInstance().getZone(event.getPlayer()).getZone(), event.getPlayer().getLocation());
        if (data == null) {
            Location loc = event.getPlayer().getLocation();
            MessageUtil.sendException(this, event.getPlayer(), "Failed to locate graveyard at " + loc.getBlockX() + ", " + loc.getBlockZ() + "!");
            event.setRespawnLocation(event.getPlayer().getLocation());
        } else {
            Location loc = new Location(data.getWorld(), data.x + (Math.random() * (data.radius * 2) - data.radius), data.y, data.z + (Math.random() * (data.radius * 2) - data.radius));
            while (loc.getBlock().getType() != Material.AIR)
                loc.add(0, 1, 0);
            event.setRespawnLocation(loc);
        }

        getScheduler().executeInSpigotCircle(() -> event.getPlayer().playSound(event.getRespawnLocation(), "misc.resurrect", 1F, 1F));

        // TODO: On death break items
		/*ItemStack[] armors = p.getEquipment().getArmorContents();
		for(ItemStack armor : armors) {
			if(armor == null || armor.getType() != Material.AIR)
				continue;

			GearController armorItem = ItemHandler.toStatItem(armor);
			if(armorItem != null) {
				ItemHandler.hurtItem(armorItem, (int)Math.floor(armorItem.getCurrentDurability() * .1));
			}else
				getLogger().severe("Attempt to break broken armor. Offender: " + p.getName());
		}
		p.getEquipment().setArmorContents(armors);
		
		ItemStack mainHand = p.getEquipment().getItemInMainHand();
		if(mainHand != null && mainHand.getType() != Material.AIR) {
			GearController heldItem = ItemHandler.toStatItem(mainHand);
			if(heldItem != null)
				p.getEquipment().setItemInMainHand(ItemHandler.hurtItem(heldItem, (int)Math.floor(heldItem.getCurrentDurability() * .1)));
			else
				getLogger().severe("Attempt to break broken mainhand item. Offender: " + p.getName());
		}
		
		ItemStack offHand = p.getEquipment().getItemInMainHand();
		if(offHand != null && offHand.getType() != Material.AIR) {
			GearController heldItem = ItemHandler.toStatItem(offHand);
			if(heldItem != null)
				p.getEquipment().setItemInOffHand(ItemHandler.hurtItem(heldItem, (int)Math.floor(heldItem.getCurrentDurability() * .1)));
			else
				getLogger().severe("Attempt to break broken offhand item. Offender: " + p.getName());
		}*/
    }

    public Graveyard getNearestGraveyard(Zone zone, Location loc) {
        if (graveyards == null || graveyards.size() == 0
                || zone == null || !graveyards.containsKey(zone.getId())) {
            return null;
        }

        Collection<Graveyard> yards = graveyards.get(zone.getId());

        Graveyard closest = null;
        double distance = Double.MAX_VALUE;
        for (Graveyard data : yards) {
            if (loc.distance(data.getLocation()) < distance) {
                closest = data;
            }
        }

        return closest;
    }

}