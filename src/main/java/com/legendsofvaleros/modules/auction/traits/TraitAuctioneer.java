package com.legendsofvaleros.modules.auction.traits;

import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.auction.Auction;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.auction.gui.AuctionGui;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Crystall on 11/25/2018
 */
public class TraitAuctioneer extends LOVTrait {

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        slot.set(new Slot(new ItemBuilder(Material.BOOK).setName("Auction House").create(), (gui, p, event) -> {
            gui.close(p);

            ListenableFuture<ArrayList<Auction>> future = AuctionController.getInstance().loadEntries();

            future.addListener(() -> {
                try {
                    ArrayList<Auction> auctions = future.get();

                    new AuctionGui(auctions).open(p);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }, AuctionController.getInstance().getScheduler()::sync);
        }));
    }
}
