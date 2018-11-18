package com.legendsofvaleros.modules.graveyard;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.zones.Zones;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;

public class Graveyards extends ListenerModule {
    private static Graveyards inst;

    public static Graveyards getInstance() {
        return inst;
    }

    @Override
    public void onLoad() {
        this.inst = inst;

        LegendsOfValeros.getInstance().getCommand("suicide").setExecutor((sender, arg1, arg2, arg3) -> {
            CombatEngine.getInstance().causeTrueDamage((Player) sender, null, CombatEngine.getEntity((Player) sender).getStats().getStat(Stat.MAX_HEALTH), ((Player) sender).getLocation());
            return true;
        });

        Utilities.getCommandManager().loadCommandClass(GraveyardCommands.class);

        GraveyardManager.onEnable();

    }

    @Override
    public void onUnload() {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerRespawnEvent event) {
        Graveyard data = GraveyardManager.getNearestGraveyard(Zones.manager().getZone(event.getPlayer()), event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockZ());
        if (data == null)
            event.setRespawnLocation(event.getPlayer().getWorld().getSpawnLocation());
        else {
            Location loc = new Location(Bukkit.getWorld(data.world), data.position[0] + (Math.random() * (data.radius * 2) - data.radius), data.position[1], data.position[2] + (Math.random() * (data.radius * 2) - data.radius));
            while (loc.getBlock().getType() != Material.AIR)
                loc.add(0, 1, 0);
            event.setRespawnLocation(loc);
        }

        Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> event.getPlayer().playSound(event.getRespawnLocation(), "misc.resurrect", 1F, 1F));

        // TODO: On death break items
		/*ItemStack[] armors = p.getEquipment().getArmorContents();
		for(ItemStack armor : armors) {
			if(armor == null || armor.getType() != Material.AIR)
				continue;

			Gear armorItem = ItemHandler.toStatItem(armor);
			if(armorItem != null) {
				ItemHandler.hurtItem(armorItem, (int)Math.floor(armorItem.getCurrentDurability() * .1));
			}else
				getLogger().severe("Attempt to break broken armor. Offender: " + p.getName());
		}
		p.getEquipment().setArmorContents(armors);
		
		ItemStack mainHand = p.getEquipment().getItemInMainHand();
		if(mainHand != null && mainHand.getType() != Material.AIR) {
			Gear heldItem = ItemHandler.toStatItem(mainHand);
			if(heldItem != null)
				p.getEquipment().setItemInMainHand(ItemHandler.hurtItem(heldItem, (int)Math.floor(heldItem.getCurrentDurability() * .1)));
			else
				getLogger().severe("Attempt to break broken mainhand item. Offender: " + p.getName());
		}
		
		ItemStack offHand = p.getEquipment().getItemInMainHand();
		if(offHand != null && offHand.getType() != Material.AIR) {
			Gear heldItem = ItemHandler.toStatItem(offHand);
			if(heldItem != null)
				p.getEquipment().setItemInOffHand(ItemHandler.hurtItem(heldItem, (int)Math.floor(heldItem.getCurrentDurability() * .1)));
			else
				getLogger().severe("Attempt to break broken offhand item. Offender: " + p.getName());
		}*/
    }
}