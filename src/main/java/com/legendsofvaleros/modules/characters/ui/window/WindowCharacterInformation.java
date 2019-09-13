package com.legendsofvaleros.modules.characters.ui.window;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.window.WindowYesNo;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.ui.CharacterSelectionListener;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class WindowCharacterInformation extends GUI {
    private ItemStack stack;

    @Override public void onOpen(Player p, InventoryView view) {
        p.getInventory().setItem(17, Models.merge("menu-ui-3x3", (stack = p.getInventory().getItem(17))));
    }

    @Override public void onClose(Player p, InventoryView view) {
        p.getInventory().setItem(17, stack);
    }

    public WindowCharacterInformation(final PlayerCharacters characters, final CharacterSelectionListener listener, final int characterId, final boolean forced) {
        super("Character Information");

        if (forced)
            fixed();

        type(InventoryType.DISPENSER);

        PlayerCharacter pChar = characters.getForNumber(characterId);

        //TODO REPLACE LEGACY ITEM
        slot(1, new ItemBuilder(Material.LEGACY_SKULL_ITEM)
                        .setName(pChar.getPlayerRace().getUserFriendlyName() + " " + pChar.getPlayerClass().getUserFriendlyName())
                        .addLore("", ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Left click" + ChatColor.DARK_GRAY + " to use]").create(),
                new SlotCharacter(characterId, characters, listener, forced));

        slot(3, new ItemBuilder(Material.WOODEN_SWORD).hideAttributes()
                .setName(null)
                .addLore(ChatColor.YELLOW + "Level: " + ChatColor.GRAY + pChar.getExperience().getLevel(),
                        ChatColor.YELLOW + "XP: " + ChatColor.GRAY + pChar.getExperience().getExperienceTowardsNextLevel()
                                + " (" + (int) Math.floor(pChar.getExperience().getPercentageTowardsNextLevel() * 100) + "%)").create(), null);

        slot(4, new ItemBuilder(Material.PAPER).setName(null).create(), null);
        slot(5, new ItemBuilder(Material.PAPER).setName(null).create(), null);


        slot(6, Models.stack("menu-arrow-left-button").create(), (gui, p, type) -> gui.close(p));

        slot(8, new ItemBuilder(Models.stack("menu-characters-delete-button").setName(ChatColor.RED + "" + ChatColor.BOLD + "Delete Character").create()).addLore("WARNING: THIS IS PERMANENT").create(), (gui, p, type) -> new WindowYesNo() {
            public void onAccept(GUI gui, Player p) {
                listener.onCharacterRemoved(p, new CharacterId(p.getUniqueId(), characterId));

                gui.close(p, Flag.NO_PARENTS);

                new WindowCharacterSelect(0, Characters.getInstance().getCharacters(p), listener, forced).open(p);
            }

            public void onDecline(GUI gui, Player p) {
                gui.close(p);
            }
        }.open(p));
    }
}