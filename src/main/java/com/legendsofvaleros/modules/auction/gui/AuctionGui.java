package com.legendsofvaleros.modules.auction.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.SlotUsable;
import com.legendsofvaleros.modules.auction.Auction;
import com.legendsofvaleros.modules.auction.AuctionChatPrompt;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.auction.filter.FilterDirection;
import com.legendsofvaleros.modules.auction.filter.FilterType;
import com.legendsofvaleros.modules.gear.item.GearItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

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
            }

            @Override
            public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                e.setCancelled(true);
            }
        });

        slot(48, filterType.getGuiMaterial(), new SlotUsable() { //change filter type
            @Override
            public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                e.setCancelled(true);
                //TODO
            }

            @Override
            public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                e.setCancelled(true);
            }
        });

        slot(48, filterDirection.getGuiMaterial(), new SlotUsable() { //change filter type
            @Override
            public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                e.setCancelled(true);
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
        //TODO check if replacing or recreating the ui is more effecient (recreating should be)
        for (int i = 0; i < AuctionGui.ITEM_COUNT_PER_PAGE; i++) {
            ItemStack slotItem = null;
            Auction auction;

            auction = getAuctionFromSlot(i);
            if (auction != null) {
                slotItem = auction.getItem().toStack();
            }

            slot(i, slotItem != null ? slotItem : new ItemStack(Material.AIR), new SlotUsable() {
                @Override
                public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                    Auction auction = getAuctionFromSlot(e.getSlot());
                    if (auction == null) return;
                    AuctionController.getInstance().startPrompt(p, auction, AuctionChatPrompt.AuctionPromptType.BUY);
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
            GearItem.Instance item = GearItem.Instance.fromStack(e.getCurrentItem());
            if (item != null) {
                AuctionController.getInstance().startPrompt(
                        (Player) e.getWhoClicked(),
                        item.getData(),
                        AuctionChatPrompt.AuctionPromptType.SELL
                );
            }
        }
    }
}
