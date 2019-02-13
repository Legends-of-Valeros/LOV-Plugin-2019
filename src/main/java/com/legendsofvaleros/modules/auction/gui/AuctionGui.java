package com.legendsofvaleros.modules.auction.gui;

import com.codingforcookies.robert.core.GUI;
import com.legendsofvaleros.modules.auction.Auction;
import com.legendsofvaleros.modules.auction.AuctionChatPrompt;
import com.legendsofvaleros.modules.auction.AuctionChatPrompt.AuctionPromptType;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.auction.filter.FilterDirection;
import com.legendsofvaleros.modules.auction.filter.FilterType;
import com.legendsofvaleros.modules.gear.item.Gear;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Crystall on 11/24/2018
 */
public class AuctionGui extends GUI implements Listener {
    private ArrayList<Auction> auctions;
    private int currentPage = 1;
    private int totalPages;
    private FilterType filterType = FilterType.REMAINING_TIME;
    private FilterDirection filterDirection = FilterDirection.DESCENDING;

    private static final int ITEM_COUNT_PER_PAGE = 36;

    public AuctionGui(ArrayList<Auction> auctions) {
        super("Auctioneer");
        type(6);
        this.auctions = auctions;
        this.totalPages = (int) Math.ceil(auctions.size() / ITEM_COUNT_PER_PAGE);
        AuctionController.getInstance().registerEvents(this);
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
            slot(45, AuctionGuiItem.PREVIOUS_PAGE.toItemStack(), (gui, p, e) -> { //previous page
                e.setCancelled(true);
                currentPage--;
                init();
            });
        }

        slot(47, AuctionGuiItem.REFRESH.toItemStack(), (gui, p, e) -> { //refresh
            e.setCancelled(true);
        });

        slot(48, filterType.toItemStack(), (gui, p, e) -> { //change filter type
            //TODO
        });

        slot(49, filterDirection.toItemStack(), (gui, p, e) -> { //change filter direction
            //TODO
        });

        if (currentPage < totalPages) {
            slot(54, AuctionGuiItem.NEXT_PAGE.toItemStack(), (gui, p, e) -> {
                currentPage++;
                init();
            });
        }

    }

    /**
     * Adds all items to the current page
     */
    private void loadItemsForPage() {
        for (int i = 0; i < AuctionGui.ITEM_COUNT_PER_PAGE; i++) {
            ItemStack slotItem = null;
            Auction auction = getAuctionFromSlot(i);

            if (auction != null) {
                slotItem = auction.getItem().toStack();
            }

            if (slotItem != null) {
                ItemMeta im = slotItem.getItemMeta();
                ArrayList<String> lore = (ArrayList<String>) im.getLore();
                lore.addAll(auction.getDescription());
                im.setLore(lore);
                slotItem.setItemMeta(im);
            }

            slot(i, slotItem != null ? slotItem : new ItemStack(Material.AIR), (gui, p, e) -> {
                Auction clickedAuction = getAuctionFromSlot(e.getSlot());
                if (clickedAuction == null) return;

                AuctionPromptType promptType = clickedAuction.isBidOffer() ? AuctionPromptType.BID : AuctionPromptType.BUY;
                AuctionController.getInstance().startPrompt(p, clickedAuction, promptType);
            });
        }
        addUIElements();
    }

    /**
     * @param slot
     * @return
     */
    private Auction getAuctionFromSlot(int slot) {
        Auction auction = null;
        try {
            auction = auctions.get(slot + (currentPage - 1) * ITEM_COUNT_PER_PAGE);
        } catch (Exception ex) { //silent catch to prevent OutOfBoundsException
        }
        return auction;
    }

    @Override
    public void onClickPlayerInventory(GUI gui, Player p, InventoryClickEvent e) {
        if (gui instanceof AuctionGui) {
            e.setCancelled(true);
            Gear.Instance item = Gear.Instance.fromStack(e.getCurrentItem());
            if (item != null) {
                e.setCurrentItem(new ItemStack(Material.AIR));
                AuctionController.getInstance().startPrompt(
                        (Player) e.getWhoClicked(),
                        item.getData(),
                        AuctionChatPrompt.AuctionPromptType.SELL
                );
            }
        }
    }
}
