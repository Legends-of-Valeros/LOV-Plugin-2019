package com.legendsofvaleros.modules.mailbox;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.SlotUsable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by Crystall on 11/24/2018
 */
public class MailboxGui extends GUI {
    ArrayList<Mail> mails;
    private int currentPage = 1;
    private int totalPages;
    private static final int ITEM_COUNT_PER_PAGE = 36;

    public MailboxGui(ArrayList<Mail> mails) {
        super("Mailbox");
        type(6);
        this.mails = mails;
        this.totalPages = (int) Math.ceil(mails.size() / 36);

        addItemsToPage();
    }

    @Override
    public void onClose(Player p, InventoryView view) {
    }

    /**
     * Adds all items to the current page
     */
    private void addItemsToPage() {
        for (int i = 0; i < MailboxGui.ITEM_COUNT_PER_PAGE; i++) {
            Mail mail = mails.get(ITEM_COUNT_PER_PAGE * currentPage);

            //TODO add paper with enchantment when unread?
            slot(i, mail.isRead() ? Material.PAPER : Material.BOOK, new SlotUsable() {
                @Override
                public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                    mail.setRead(true);
                    //TODO create book with text of the item and hoverable texts (for example for the item)
                }

                @Override
                public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                }
            });
        }
        addUIElements();
    }

    private void addUIElements() {
        if (currentPage > 1) {
            slot(45, Material.PAPER, new SlotUsable() { //previous page
                @Override
                public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                    //TODO previous page
                }

                @Override
                public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                }
            });
        }

        if (currentPage < totalPages) {
            slot(54, Material.PAPER, new SlotUsable() { //next page
                @Override
                public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                    //TODO next page
                }

                @Override
                public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                }
            });
        }
    }

}
