package com.legendsofvaleros.modules.mailbox.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.SlotUsable;
import com.legendsofvaleros.modules.mailbox.Mail;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Crystall on 11/24/2018
 */
public class MailboxGui extends GUI {
    private ArrayList<Mail> mails;
    private int currentPage = 1;
    private int totalPages;
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
            ItemStack slotItem = new ItemStack(Material.AIR);
            Mail mail = getMailFromSlot(i);

            if (mail != null) {
                slotItem = new ItemStack(mail.isRead() ? Material.PAPER : Material.BOOK);
                ItemMeta im = slotItem.getItemMeta();
                ArrayList<String> lore = (ArrayList<String>) im.getLore();
                lore.add(mail.getContent());
                im.setLore(lore);
                slotItem.setItemMeta(im);
            }

            slot(i, slotItem, (gui, p, e) -> {
                Mail clickedMail = getMailFromSlot(e.getSlot());
                if (clickedMail == null) return;
                clickedMail.setRead(true);
                MailboxController.getInstance().openMail(clickedMail);
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
