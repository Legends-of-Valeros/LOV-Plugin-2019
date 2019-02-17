package com.legendsofvaleros.modules.mailbox.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.SlotUsable;
import com.legendsofvaleros.modules.auction.filter.FilterDirection;
import com.legendsofvaleros.modules.auction.filter.FilterType;
import com.legendsofvaleros.modules.mailbox.Mail;
import com.legendsofvaleros.modules.mailbox.MailboxController;
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
    private ArrayList<Mail> mails;
    private int currentPage = 1;
    private int totalPages;
    private FilterType filterType = FilterType.REMAINING_TIME;
    private FilterDirection filterDirection = FilterDirection.DESCENDING;

    private static final int ITEM_COUNT_PER_PAGE = 36;

    public MailboxGui(ArrayList<Mail> mails) {
        super("Mailbox");
        type(6);
        this.mails = mails;
        this.totalPages = (int) Math.ceil(mails.size() / ITEM_COUNT_PER_PAGE);
        this.init();
    }

    @Override
    public void onClose(Player p, InventoryView view) {
    }

    private void init() {
        loadItemsForPage();
        addUIElements();
    }

    private void addUIElements() {
        if (currentPage > 1) {
            slot(45, Material.PAPER, new SlotUsable() { //previous page
                @Override
                public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                    currentPage--;
                    init();
                }

                @Override
                public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                }
            });
        }

        slot(47, Material.GREEN_RECORD, new SlotUsable() { //refresh
            @Override
            public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                e.setCancelled(true);
                init();
            }

            @Override
            public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                e.setCancelled(true);
            }
        });

        if (currentPage < totalPages) {
            slot(54, Material.PAPER, new SlotUsable() { //next page
                @Override
                public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                    currentPage++;
                    init();
                }

                @Override
                public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                }
            });
        }
    }


    /**
     * Adds all items to the current page
     */
    private void loadItemsForPage() {
        for (int i = 0; i < MailboxGui.ITEM_COUNT_PER_PAGE; i++) {
            Mail mail = getMailFromSlot(i);
            //TODO add paper with enchantment when unread?
            slot(i, mail != null ? (mail.isRead() ? Material.PAPER : Material.BOOK) : Material.AIR, new SlotUsable() {
                @Override
                public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                    mail.setRead(true);
                    MailboxController.getInstance().openMail(mail);
                }

                @Override
                public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                }
            });
        }
        addUIElements();
    }

    /**
     * @param slot
     * @return
     */
    private Mail getMailFromSlot(int slot) {
        Mail mail = null;
        try {
            mail = mails.get(slot + (currentPage - 1) * ITEM_COUNT_PER_PAGE);
        } catch (Exception ex) { //silent catch to prevent OutOfBoundsException
        }
        return mail;
    }
}
