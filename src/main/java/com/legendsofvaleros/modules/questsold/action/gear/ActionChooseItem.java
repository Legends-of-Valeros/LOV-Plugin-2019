package com.legendsofvaleros.modules.questsold.action.gear;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.ISlotAction;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.questsold.action.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class ActionChooseItem extends AbstractQuestAction {
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
	public void play(PlayerCharacter pc, Next next) {
		Gear[] items = new Gear[itemIds.length];

		for(int i = 0; i < itemIds.length; i++)
			items[i] = Gear.fromId(itemIds[i]);

		try {
			GUI gui = new GUI("Choose One") {
				private ItemStack stack;
				@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge("menu-ui-chest-3", (stack = p.getInventory().getItem(17)))); }
				@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }
			};

			gui.type(3);
			gui.fixed();

			for(int i = 0; i < items.length; i++) {
				Gear.Instance instance = items[i].newInstance();
				instance.amount = amounts[i] == null ? 1 : amounts[i];

				ISlotAction action = (ui, p, event) -> {
					ui.close(p);

					MessageUtil.sendUpdate(pc.getPlayer(), new TextBuilder("You received " + (instance.amount == 1 ? "a " : instance.amount + "x") + "[").color(ChatColor.AQUA)
							.append(instance.getName()).color(ChatColor.GREEN)
							.append("]!").color(ChatColor.AQUA).create());

					ItemUtil.giveItem(pc, instance);

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

			gui.open(pc.getPlayer());
		} catch(Exception e) {
			MessageUtil.sendException(GearController.getInstance(), pc.getPlayer(), e);
		}
	}
}