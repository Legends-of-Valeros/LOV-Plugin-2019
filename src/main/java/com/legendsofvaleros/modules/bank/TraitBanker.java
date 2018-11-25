package com.legendsofvaleros.modules.bank;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.codingforcookies.robert.slot.SlotUsable;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TraitBanker extends LOVTrait {
	@Override
	public void onRightClick(NPC npc, Player player, SettableFuture<Slot> slot) {
		if(!Characters.isPlayerCharacterLoaded(player)) {
			slot.set(null);
			return;
		}
		
		slot.set(new Slot(new ItemBuilder(Material.IRON_FENCE).setName("Banking").create(), (gui, p, event) -> {
			gui.close(p);

			new BankView(Bank.getBank(Characters.getPlayerCharacter(p))).open(p);
		}));
	}
	
	private class BankView extends GUI {
		PlayerBank bank;
		
		BankView(PlayerBank bank) {
			super("Bank");
			
			this.bank = bank;
			
			type(6);

			for(int i = 0; i < 6 * 9; i++) {
				PlayerBank.Entry entry = bank.content.get(i);
				GearItem.Data item = entry != null ? entry.item : null;

				int j = i;
				slot(i, item != null ? item.toStack() : null, new SlotUsable() {
					@Override
					public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent event) {
						if(stack.getType() != Material.AIR)
							bank.removeItem(j);
					}

					@Override
					public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent event) {
						GearItem.Instance instance = GearItem.Instance.fromStack(stack);
						if(instance == null) {
							event.setCancelled(true);
							return;
						}

						bank.setItem(j, instance.getData());
					}
				});
			}
		}
	}
}