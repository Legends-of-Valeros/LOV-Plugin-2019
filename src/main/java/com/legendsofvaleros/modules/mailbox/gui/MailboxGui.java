package com.legendsofvaleros.modules.mailbox.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.core.GuiItem;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.mailbox.Mail;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Crystall on 11/24/2018
 */
public class MailboxGui extends GUI implements Listener {
    private ArrayList<Mail> mails;
    private int currentPage = 1;
    private int totalPages;
    private static final int ITEM_COUNT_PER_PAGE = 36;

    public MailboxGui(ArrayList<Mail> mails) {
        super("Mailbox");
        this.mails = mails;
        this.totalPages = (int) Math.ceil(((double) mails.size()) / ((double) ITEM_COUNT_PER_PAGE));
        MailboxController.getInstance().registerEvents(this);
        type(6);
        this.loadItemsForPage();
    }

    @Override
    public void onClose(Player p, InventoryView view) {
    }

    private void addUIElements() {
        if (currentPage > 1) {
            slot(45, GuiItem.PREVIOUS_PAGE.toItemStack(), (gui, p, e) -> { //previous page
                currentPage--;
                this.loadItemsForPage();
            });
        } else {
            getInventory().setItem(45, new ItemStack(Material.AIR));
        }

        slot(47, GuiItem.REFRESH.toItemStack(), (gui, p, e) -> { //refresh
            this.loadItemsForPage();
        });

        if (currentPage < totalPages) {
            slot(53, GuiItem.NEXT_PAGE.toItemStack(), (gui, p, e) -> {
                currentPage++;
                this.loadItemsForPage();
            });
        } else {
            getInventory().setItem(53, new ItemStack(Material.AIR));
        }
    }

    /**
     * Adds all items to the current page
     */
    private void loadItemsForPage() {
        int firstOnPage = (currentPage - 1) * MailboxGui.ITEM_COUNT_PER_PAGE;
        int lastOnPage = Math.min(currentPage * MailboxGui.ITEM_COUNT_PER_PAGE, AuctionController.getInstance().auctions.size());
        getInventory().clear();
        for (int i = firstOnPage; i < lastOnPage; i++) {
            Mail mail = getMailFromSlot(i);
            int slotIndex = i - ((currentPage - 1) * MailboxGui.ITEM_COUNT_PER_PAGE);

            if (mail == null) {
                continue;
            }

            ItemStack slotItem = new ItemStack(mail.isRead() ? Material.PAPER : Material.BOOK);
            ItemMeta im = slotItem.getItemMeta();
            im.setDisplayName(mail.getTitle());
            ArrayList<String> lore = new ArrayList<>();
            lore.add(mail.getContent());
            im.setLore(lore);
            slotItem.setItemMeta(im);

            slot(slotIndex, slotItem, (gui, p, e) -> {
                Mail clickedMail = getMailFromSlot(e.getSlot() + (currentPage - 1) * MailboxGui.ITEM_COUNT_PER_PAGE);
                if (clickedMail == null) {
                    return;
                }
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
            mail = mails.get(slot);
        } catch (Exception ex) { //silent catch to prevent OutOfBoundsException
        }
        return mail;
    }

}
