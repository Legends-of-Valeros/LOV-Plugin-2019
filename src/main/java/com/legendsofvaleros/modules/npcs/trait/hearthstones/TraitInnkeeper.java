package com.legendsofvaleros.modules.npcs.trait.hearthstones;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.codingforcookies.robert.window.WindowYesNo;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.hearthstones.HearthstoneController;
import com.legendsofvaleros.modules.hearthstones.core.HomePoint;
import com.legendsofvaleros.modules.npcs.core.NPCEmulator;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class TraitInnkeeper extends LOVTrait {
	private static final double SAME_LOC_DISTANCE = 12.0;

	public String name = "Undefined";

	@Override
	public void onRightClick(Player player, SettableFuture<Slot> slot) {
		if(!Characters.isPlayerCharacterLoaded(player)) {
			slot.set(null);
			return;
		}
		
		slot.set(new Slot(new ItemBuilder(Material.NETHER_STAR).setName("Innkeeper").create(), (gui, p, event) -> {
			gui.close(p);
			
			openGUI(p);
		}));
	}
	
	public void openGUI(Player p) {
		PlayerCharacter pc = Characters.getPlayerCharacter(p);
		
		final Location loc = p.getLocation();

		HomePoint oldHome = HearthstoneController.getInstance().getHome(pc);
		if(oldHome != null && loc.distance(oldHome.getLocation()) <= SAME_LOC_DISTANCE)
			NPCEmulator.speak(trait.getLovNPC(), p, "You already have a room here!");
		else{
			new WindowYesNo("Reserve a room?") {
				@Override
				public void onAccept(GUI gui, Player p) {
					HearthstoneController.getInstance().setHome(pc, name, loc);
					MessageUtil.sendUpdate(p, "Home set. Select the Hearthstone in your player menu to teleport back here!");
					p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
				}

				@Override
				public void onDecline(GUI gui, Player p) {
					MessageUtil.sendError(p, "Home not set.");
				}
			}.open(p);
		}
	}
}