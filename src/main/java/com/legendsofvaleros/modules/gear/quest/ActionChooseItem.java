package com.legendsofvaleros.modules.gear.quest;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.ISlotAction;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.item.Model;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class ActionChooseItem extends AbstractAction {
	String itemId_1, itemId_2, itemId_3, itemId_4, itemId_5;
	int amount_1, amount_2, amount_3, amount_4, amount_5;

	String[] itemIds;
	Integer[] amounts;

	public void onInit() {
		int i = 0;
		if(amount_1 > 0) i++;
		if(amount_2 > 0) i++;
		if(amount_3 > 0) i++;
		if(amount_4 > 0) i++;
		if(amount_5 > 0) i++;
		
		itemIds = new String[i];
		amounts = new Integer[i];

		if(amount_1 > 0) { itemIds[0] = itemId_1; amounts[0] = amount_1; }
		if(amount_2 > 0) { itemIds[1] = itemId_2; amounts[1] = amount_2; }
		if(amount_3 > 0) { itemIds[2] = itemId_3; amounts[2] = amount_3; }
		if(amount_4 > 0) { itemIds[3] = itemId_4; amounts[3] = amount_4; }
		if(amount_5 > 0) { itemIds[4] = itemId_5; amounts[4] = amount_5; }
	}
	
	@Override
	public void play(Player player, Next next) {
		GearItem[] items = new GearItem[itemIds.length];

		for(int i = 0; i < itemIds.length; i++)
			items[i] = GearItem.fromID(itemIds[i]);

		try {
			GUI gui = new GUI("Choose One") {
				private ItemStack stack;
				@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
				@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }
			};

			gui.type(3);
			gui.fixed();

			for(int i = 0; i < items.length; i++) {
				GearItem.Instance instance = items[i].newInstance();
				instance.amount = amounts[i] == null ? 1 : amounts[i];

				ISlotAction action = (ui, p, event) -> {
					ui.close(p);

					MessageUtil.sendUpdate(player, new FancyMessage("You received " + (instance.amount == 1 ? "a " : instance.amount + "x") + "[").color(ChatColor.AQUA)
							.then(instance.gear.getName()).color(ChatColor.GREEN)
							.then("]!").color(ChatColor.AQUA));

					ItemUtil.giveItem(Characters.getPlayerCharacter(player), instance);

					next.go();
				};

				if(items.length == 1)
					gui.slot(4, 1, instance.toStack(), action);
				else if(items.length == 2)
					gui.slot(i == 0 ? 1 : 3, 1, instance.toStack(), action);
				else if(items.length == 3)
					gui.slot(2 + i * 2, 1, instance.toStack(), action);
				else if(items.length == 4)
					gui.slot(1 + i * 2, 1, instance.toStack(), action);
				else
					gui.slot(2 + i, 1, instance.toStack(), action);
			}

			gui.open(player);
		} catch(Exception e) {
			MessageUtil.sendException(Gear.getInstance(), player, e, false);
		}
	}
}