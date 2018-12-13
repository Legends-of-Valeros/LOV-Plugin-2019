package com.legendsofvaleros.modules.fast_travel;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.NPCData;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.npcs.util.NPCEmulator;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TraitFastTravel extends LOVTrait {
	public static class Data {
		public int cost;
		public String message;
	}
	
	public String name;

	public HashMap<String, TraitFastTravel.Data> connections;

	@Override
	public void onRightClick(Player player, SettableFuture<Slot> slot) {
		if(!Characters.isPlayerCharacterLoaded(player)) {
			slot.set(null);
			return;
		}
		
		Collection<String> found = DiscoveredFastTravels.getDiscovered(Characters.getPlayerCharacter(player));

		if(!found.contains(npc_id)) {
			found.add(npc_id);

			DiscoveredFastTravels.add(Characters.getPlayerCharacter(player), npc_id);

			Title title = new Title("Discovered Location!", "You can now fast travel here!");
			title.setTitleColor(ChatColor.GREEN);
			title.setSubtitleColor(ChatColor.YELLOW);
			TitleUtil.queueTitle(title, player);
		}

		List<String> available = new ArrayList<>();

		for(String id : found)
			if(connections.containsKey(id))
				available.add(id);

		if(available.size() == 0) {
			slot.set(null);
			return;
		}

		slot.set(new Slot(new ItemBuilder(Material.LEATHER_BOOTS).setName("Fast Travel").create(), (gui, p, event) -> openGUI(p, available)));
	}
	
	private void openGUI(Player p, Collection<String> available) {
		GUI gui = new GUI(npc.getName());
		gui.type(InventoryType.DISPENSER);
		
		int i = 0;
		for(String id : available) {
			TraitFastTravel.Data data = connections.get(id);
			
			NPCData npcData = NPCs.getNPC(id);
			if(npc == null) {
				MessageUtil.sendError(p, "Unable to find NPC with that ID: " + id);
				continue;
			}
			
			TraitFastTravel trait = npcData.getTrait(TraitFastTravel.class);
			if(trait == null) {
				MessageUtil.sendError(p, "Destination NPC does not have fast travel name set: " + id);
				continue;
			}
			
			ItemBuilder ib = new ItemBuilder(Material.EYE_OF_ENDER);

			if(trait.name == null || trait.name.length() == 0)
				ib.setName("- Unnamed -");
			else
				ib.setName(trait.name);
			ib.addLore("", "" + Money.Format.format(data.cost));
			
			gui.slot(i++, ib.create(), (gui1, p1, event) -> {
                if(!Money.sub(Characters.getPlayerCharacter(p1), data.cost)) {
                    NPCEmulator.speak(npc, p1, "You don't have enough crowns for that.");
                    return;
                }

                p1.teleport(npcData.loc);

                if(data.message != null && data.message.length() > 0)
                    NPCEmulator.speak(npc, p1, data.message);
            });
		}
		
		gui.open(p);
	}
}