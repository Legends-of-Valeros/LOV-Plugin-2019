package com.legendsofvaleros.modules.characters.ui.window;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.ui.CharacterSelectionListener;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class WindowCharacterSelect extends GUI {
	private ItemStack stack;
	@Override public void onOpen(Player p, InventoryView view) { p.getInventory().setItem(17, Model.merge(isFixed() ? "menu-ui-character-select" : "menu-ui-3x3", (stack = p.getInventory().getItem(17)))); }
	@Override public void onClose(Player p, InventoryView view) { p.getInventory().setItem(17, stack); }

	public WindowCharacterSelect(final int page, final PlayerCharacters characters, final CharacterSelectionListener listener, final boolean forced) {
		super("Character Select #" + (page + 1));
		
		if(forced)
			fixed();
		
		type(InventoryType.DISPENSER);
		
		if(forced)
			slot(7, Model.stack("menu-logout-button").setName(ChatColor.RED + "Logout").create(), (gui, p, event) -> {
                gui.close(p);

                p.kickPlayer("Logged out.");
            });
		
		slot(6, Model.stack("menu-arrow-left-button").setEnchanted(page > 0)
				.setName((page <= 0 ? ChatColor.RED : ChatColor.YELLOW) + "< Page").create(), (gui, p, event) -> {
                    if(page > 0) {
                        new WindowCharacterSelect(page - 1, characters, listener, forced).open(p, Flag.NO_PARENTS);
                    }
                });
		
		final int len = characters.getMaxCharacters();
		
		slot(8, Model.stack("menu-arrow-right-button").setEnchanted(len - page * 6 >= 6)
				.setName((len < page * 6 ? ChatColor.RED : ChatColor.YELLOW) + "Page >").create(), (gui, p, event) -> {
                    if(len - page * 6 > 6) {
                        new WindowCharacterSelect(page + 1, characters, listener, forced).open(p, Flag.NO_PARENTS);
                    }
                });
		
		for(int i = 0; i < (len - page * 6 > 6 ? 6 : len - page * 6); i++) {
			final PlayerCharacter pChar = characters.getForNumber(page * 6 + i);
			
			if(pChar == null) {
				ItemBuilder ib = Model.stack("menu-characters-new-button")
						.setName("New Character")
						.addLore("", ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Left click" + ChatColor.DARK_GRAY + " to create a new character]");
				slot(i, ib.create(), new SlotNewCharacter(page * 6 + i, listener));
				continue;
			}
			
			ItemBuilder ib = Model.stack("menu-characters-button")
								.setName(pChar.getPlayerRace().getUserFriendlyName() + " " + pChar.getPlayerClass().getUserFriendlyName())
								.addLore("", ChatColor.GOLD + "" + ChatColor.BOLD + "Level: " +
												ChatColor.GRAY + pChar.getExperience().getLevel(),
												"", ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Left Click" + ChatColor.DARK_GRAY + " to Use]",
												ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Right Click" + ChatColor.DARK_GRAY + " for More Info]");
			
			if(pChar.isCurrent())
				ib.addLore("", ChatColor.ITALIC + "" + ChatColor.YELLOW + "Current character");
			
			slot(i, ib.create(), new SlotCharacter(i, characters, listener, forced));
		}
	}
}