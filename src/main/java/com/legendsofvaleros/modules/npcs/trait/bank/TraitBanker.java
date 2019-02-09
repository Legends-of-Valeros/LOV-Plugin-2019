package com.legendsofvaleros.modules.npcs.trait.bank;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.codingforcookies.robert.slot.SlotUsable;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.bank.core.Bank;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TraitBanker extends LOVTrait {
	@Override
	public void onRightClick(Player player, SettableFuture<Slot> slot) {
		if(!Characters.isPlayerCharacterLoaded(player)) {
			slot.set(null);
			return;
		}
		
		slot.set(new Slot(new ItemBuilder(Material.IRON_FENCE).setName("Banking").create(), (gui, p, event) -> {
			gui.close(p);

			new BankView(BankController.getBank(Characters.getPlayerCharacter(p))).open(p);
		}));
	}
	
	private class BankView extends GUI {
		Bank bank;
		
		BankView(Bank bank) {
			super("Bank");
			
			this.bank = bank;
			
			type(6);

			for(int i = 0; i < 6 * 9; i++) {
				Gear.Data item = bank.getContent().get(i);

				int j = i;
				slot(i, item != null ? item.toStack() : null, new SlotUsable() {
					@Override
					public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent event) {
						if(stack.getType() != Material.AIR)
							bank.removeItem(j);
					}

					@Override
					public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent event) {
						Gear.Instance instance = Gear.Instance.fromStack(stack);
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