package com.legendsofvaleros.modules.bank.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.gear.component.bank.WorthComponent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class CreatePouchGUI extends GUI {
	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }

	/*private static final ItemBuilder up = Model.stack("menu-arrow-up-button");
	private static final ItemBuilder down = Model.stack("menu-arrow-down-button");*/
	private static final ItemBuilder[] number = new ItemBuilder[10];
	
	static {
		for(int i = 0; i <= 9; i++)
			number[i] = Model.stack("number-" + i);
	}
	
	private byte[] amount = new byte[9];

	public CreatePouchGUI() {
		super("Withdrawal");
		
		type(3);

		{
			updateButton(new int[] { 0, 0 }, 0);
			updateButton(new int[] { 1, 0 }, 1);
			updateButton(new int[] { 2, 0 }, 2);
			updateButton(new int[] { 3, 0 }, 3);
			updateButton(new int[] { 4, 0 }, 4);
			
			slot(5, 0, Model.stack("crowns-gold").create(), null);
		}

		{
			updateButton(new int[] { 0, 2 }, 5);
			updateButton(new int[] { 1, 2 }, 6);

			slot(2, 2, Model.stack("crowns-silver").create(), null);
		}

		{
			updateButton(new int[] { 3, 2 }, 7);
			updateButton(new int[] { 4, 2 }, 8);

			slot(5, 2, Model.stack("crowns-copper").create(), null);
		}

		slot(7, 1, Material.AIR, (gui, p, event) -> {
			ItemStack stack = getAmountStack(p);
			if(stack == null) return;
			
			event.getView().setCursor(stack);
		});
		
		updateAmountStack();
	}
	
	private void updateButton(int[] pos, int i) {
		/*
		int j = (int)Math.floor(i * 1.5);
		slot(j, 0, up.setEnchanted(amount[i] < 9).create(), amount[i] == 9 ? null : (gui, p, event) -> {
			
		});*/
		
		slot(pos[0], pos[1], number[amount[i]].create(), (gui, p, event) -> {
			if(event.isLeftClick()) {
				if(amount[i] < 9) {
					amount[i]++;
					updateButton(pos, i);
				}
			}else if(event.isRightClick()) {
				if(amount[i] > 0) {
					amount[i]--;
					updateButton(pos, i);
				}
			}
			
			updateAmountStack();
		});
		
		/*slot(j, 2, down.setEnchanted(amount[i] > 0).create(), amount[i] == 0 ? null : (gui, p, event) -> {
			amount[i]--;
			updateButton(i);
		});*/
	}
	
	private void updateAmountStack() {
		getInventory().setItem(16, getAmountStack(null));
	}
	
	private ItemStack getAmountStack(Player p) {
		try {
			StringBuilder str = new StringBuilder();
			for(byte anAmount : amount) str.append(anAmount);
			
			long amount = Long.parseLong(str.toString());
			if(p != null) {
				PlayerCharacter pc = Characters.getPlayerCharacter(p);

				if(amount == 0)
					amount = Money.get(pc);
				
				if(amount <= 0) return null;

				if(amount > Money.get(pc)
						|| !Money.sub(pc, amount))
					return null;
				onOpen(p, getView(p));
			}
		
			Gear.Instance instance = Gear.Instance.fromID("crowns-pouch");
			if(instance == null) return null;
			
			instance.putPersist(WorthComponent.class, amount);
			return instance.toStack();
		} catch (Exception e) {
			MessageUtil.sendException(BankController.getInstance(), p, e, true);
		}
		
		return null;
	}
}