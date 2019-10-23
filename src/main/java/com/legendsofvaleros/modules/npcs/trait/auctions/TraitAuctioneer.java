package com.legendsofvaleros.modules.npcs.trait.auctions;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.features.gui.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.auction.gui.AuctionGui;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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

            new AuctionGui(AuctionController.getInstance().auctions).open(p);
        }));
    }
}
