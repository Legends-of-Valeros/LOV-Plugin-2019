package com.legendsofvaleros.modules.auction.gui;

import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.core.GuiItem;
import com.legendsofvaleros.modules.auction.Auction;
import com.legendsofvaleros.modules.auction.AuctionChatPrompt.AuctionPromptType;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.auction.filter.FilterDirection;
import com.legendsofvaleros.modules.auction.filter.FilterType;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Crystall on 11/24/2018
 */
public class AuctionGui extends GUI implements Listener {
    private int currentPage = 1;
    private int totalPages;
    private FilterType filterType = FilterType.REMAINING_TIME;
    private FilterDirection filterDirection = FilterDirection.DESCENDING;
    private static final int ITEM_COUNT_PER_PAGE = 36;

    public AuctionGui(ArrayList<Auction> auctions) {
        super("Auctioneer");
        this.totalPages = (int) Math.ceil(((double) auctions.size()) / ((double) ITEM_COUNT_PER_PAGE));
        AuctionController.getInstance().registerEvents(this);
        type(6);
        this.loadItemsForPage();
    }

    private void addUIElements() {
        if (currentPage > 1) {
            slot(45, GuiItem.PREVIOUS_PAGE.toItemStack(), (gui, p, e) -> { //previous page
                e.setCancelled(true);
                currentPage--;
                this.loadItemsForPage();
            });
        } else {
            getInventory().setItem(45, new ItemStack(Material.AIR));
        }

        slot(47, GuiItem.REFRESH.toItemStack(), (gui, p, e) -> { //refresh
            this.loadItemsForPage();
        });

        slot(48, filterType.toItemStack(), (gui, p, e) -> { //change filter type
            //TODO
        });

        slot(49, filterDirection.toItemStack(), (gui, p, e) -> { //change filter direction
            //TODO
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
        int firstOnPage = (currentPage - 1) * AuctionGui.ITEM_COUNT_PER_PAGE;
        int lastOnPage = Math.min(currentPage * AuctionGui.ITEM_COUNT_PER_PAGE, AuctionController.getInstance().auctions.size());
        getInventory().clear();
        for (int i = firstOnPage; i < lastOnPage; i++) {
            Auction auction = getAuctionFromSlot(i);
            int slotIndex = i - ((currentPage - 1) * AuctionGui.ITEM_COUNT_PER_PAGE);

            if (auction == null) {
                continue;
            }

            ItemStack slotItem = auction.getItem().toStack();
            if (slotItem == null) {
                continue;
            }

            ItemMeta im = slotItem.getItemMeta();
            ArrayList<String> lore = (ArrayList<String>) im.getLore();
            lore.addAll(auction.getDescription());
            im.setLore(lore);
            slotItem.setItemMeta(im);

            slot(slotIndex, slotItem, (gui, p, e) -> {
                Auction clickedAuction = getAuctionFromSlot(e.getSlot() + (currentPage - 1) * AuctionGui.ITEM_COUNT_PER_PAGE);
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
            auction = AuctionController.getInstance().auctions.get(slot);
        } catch (Exception ex) { //silent catch to prevent OutOfBoundsException
        }
        return auction;
    }

    @Override
    public void onClickPlayerInventory(GUI gui, Player p, InventoryClickEvent e) {
        if (gui instanceof AuctionGui) {
            e.setCancelled(true);
            Gear.Instance item = Gear.Instance.fromStack(e.getCurrentItem());
            if (item == null) {
                return;
            }
            if (!item.gear.getType().isTradable()) {
                MessageUtil.sendError(p, "You can't auction this item!");
                return;
            }
            if (AuctionController.getInstance().isPrompted(p)) {
                MessageUtil.sendError(p, "You are already selling an item.");
                return;
            }
            e.setCurrentItem(new ItemStack(Material.AIR));
            AuctionController.getInstance().startPrompt(
                    (Player) e.getWhoClicked(),
                    item.getData(),
                    AuctionPromptType.SELL
            );
        }
    }
}
