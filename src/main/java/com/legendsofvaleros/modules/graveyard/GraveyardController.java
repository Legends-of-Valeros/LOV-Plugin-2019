package com.legendsofvaleros.modules.graveyard;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.graveyard.commands.GraveyardCommands;
import com.legendsofvaleros.modules.graveyard.core.Graveyard;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;

@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(GearController.class)
@DependsOn(ZonesController.class)
// TODO: Create subclass for listeners?
@ModuleInfo(name = "Graveyards", info = "")
public class GraveyardController extends ModuleListener {
    private static GraveyardController instance;
    public static GraveyardController getInstance() { return instance; }

    private GraveyardAPI api;
    public GraveyardAPI getApi() { return api; }

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        this.api = new GraveyardAPI();

        LegendsOfValeros.getInstance().getCommand("suicide").setExecutor((sender, arg1, arg2, arg3) -> {
            if(CombatEngine.getEntity((Player)sender) == null) return false;

            CombatEngine.getInstance().causeTrueDamage((Player) sender, null, CombatEngine.getEntity((Player) sender).getStats().getStat(Stat.MAX_HEALTH), ((Player) sender).getLocation());
            return true;
        });

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new GraveyardCommands());
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        try {
            this.api.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerRespawnEvent event) {
        Graveyard data = api.getNearestGraveyard(ZonesController.getManager().getZone(event.getPlayer()), event.getPlayer().getLocation());
        if (data == null) {
            Location loc = event.getPlayer().getLocation();
            MessageUtil.sendException(this, event.getPlayer(), "Failed to locate graveyard at " + loc.getBlockX() + ", " + loc.getBlockZ() + "!");
            event.setRespawnLocation(event.getPlayer().getLocation());
        }else{
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
}