package com.legendsofvaleros.modules.auction.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.SlotUsable;
import com.legendsofvaleros.modules.auction.Auction;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.auction.filter.FilterDirection;
import com.legendsofvaleros.modules.auction.filter.FilterType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by Crystall on 11/24/2018
 */
public class AuctionGui extends GUI {
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
        this.totalPages = (int) Math.ceil(auctions.size() / 36);

        addItemsToPage(1);
    }

    @Override
    public void onClose(Player p, InventoryView view) {
    }

    private void addUIElements() {
        if (currentPage > 1) {
            slot(45, Material.PAPER, new SlotUsable() { //previous page
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
        }

        slot(47, Material.GREEN_RECORD, new SlotUsable() { //refresh
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
     * @param page
     */
    private void addItemsToPage(int page) {
        for (int i = 0; i < AuctionGui.ITEM_COUNT_PER_PAGE; i++) {
            Auction auction = auctions.get(ITEM_COUNT_PER_PAGE * currentPage);

            slot(i, auction.getItem() != null ? auction.getItem().newInstance().getData().toStack() : null, new SlotUsable() {
                @Override
                public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                    AuctionController.getInstance().startBuyPrompt(p, auction);
                }

                @Override
                public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent e) {
                    e.setCancelled(true);
                }
            });
        }
        addUIElements();
    }
}
