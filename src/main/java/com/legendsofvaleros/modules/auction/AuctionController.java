package com.legendsofvaleros.modules.auction;

import com.codingforcookies.doris.orm.ORMTable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.auction.traits.TraitAuctioneer;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.scheduler.InternalTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Crystall on 10/10/2018
 */
@DependsOn(NPCs.class)
@DependsOn(Gear.class)
@DependsOn(Characters.class)
public class AuctionController extends ModuleListener {

    private static AuctionController instance;

    //TODO create prompt helper or check for existing
    // arraylist of players that are asked if they want to buy the clicked item
    public HashMap<Player, Auction> chatBuyPrompt = new HashMap<>();

    // arraylist of players that are asked if they want to buy the clicked item
    public HashMap<Player, Auction> chatBidPrompt = new HashMap<>();

    // arraylist of players that are asked for how much they want to sell the item
    public HashMap<Player, Auction> pricePrompt = new HashMap<>();

    // TODO add caching of inventory / filter
    private static final Cache<String, Inventory> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .maximumSize(1024)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    private static ORMTable<Auction> auctionsTable;

    public static AuctionController getInstance() {
        return AuctionController.instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        String dbPoolId = LegendsOfValeros.getInstance().getConfig().getString("dbpools-database");
        auctionsTable = ORMTable.bind(dbPoolId, Auction.class);
        NPCs.registerTrait("auctioneer", TraitAuctioneer.class);

        //TODO add scheduler that checks every second if an item has run out and if it is an bid item
    }

    public void onUnload() {
        super.onUnload();
    }

    public void startBuyPrompt(Player p, Auction auction) {
        if (chatBuyPrompt.containsKey(p)) {
            //TODO add item with hover
            p.sendMessage("You are already in a buy prompt for an item");
            return;
        }
        p.closeInventory();
        chatBuyPrompt.put(p, auction);
        // TODO add ChatComponent with hoverable item
        // p.spigot().sendMessage();
        p.sendMessage(ChatColor.DARK_BLUE + "Do you really want to buy this Item?");
    }

    /**
     * Enters a bid prompt with a player
     * @param p
     * @param auction
     */
    public void startBidPrompt(Player p, Auction auction) {
        if (chatBidPrompt.containsKey(p)) {
            //TODO add item with hover
            p.sendMessage("You are already in a buy prompt for an item");
            return;
        }
        p.closeInventory();
        chatBidPrompt.put(p, auction);
        // TODO add ChatComponent with hoverable item
        // p.spigot().sendMessage();
        p.sendMessage(ChatColor.DARK_BLUE + "How much do you want to bid on the item?");
    }

    public ArrayList<Auction> loadEntries() {
        ArrayList<Auction> auctions = new ArrayList<>();

        getScheduler().executeInMyCircle(new InternalTask(() -> {
            auctionsTable.query()
                    .all()
                    .forEach((entry) -> auctions.add(entry))
                    .execute(true);
        }));

        return auctions;
    }

    public void addAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() ->
                auctionsTable.query().insert().values(
                        "owner_id", auction.getOwnerId(),
                        "auction_item", auction.getItem().newInstance().toString(),
                        "valid_until", auction.getValidUntil(),
                        "is_bid_offer", auction.isBidOffer(),
                        "price", auction.getPrice()
                ).build().execute(true)));
    }

    public void removeAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            auctionsTable.query()
                    .remove()
                    .where("id", auction.getId())
                    .build()
                    .execute(true);
        }));
    }

    /**
     * Query for security reason, to make sure, that the item did not get bought by another player
     * @param auction
     * @return
     */
    public ListenableFuture<Boolean> checkIfAuctionStillExists(Auction auction) {
        SettableFuture<Boolean> ret = SettableFuture.create();

        if (!ret.isDone()) {
            getScheduler().executeInMyCircle(new InternalTask(() -> {
                auctionsTable.query()
                        .select()
                        .where("id", auction.getId())
                        .build()
                        .callback((result) -> {
                            if (!result.next()) {
                                ret.set(false);
                                return;
                            }
                            ret.set(true);
                        }).execute(true);
            }));
        }

        return ret;
    }

    @EventHandler
    public void OnChatEvent(AsyncPlayerChatEvent e) {
        if (chatBuyPrompt.containsKey(e.getPlayer())) {
            if (!e.isCancelled()) {
                e.setCancelled(true);
                e.setMessage("");
            }

            Auction auction = chatBuyPrompt.get(e.getPlayer());
            //Player did accept to buy the item
            if (!e.getMessage().equalsIgnoreCase("y") || e.getMessage().equalsIgnoreCase("yes")) {
                chatBuyPrompt.remove(e.getPlayer());
                //TODO make pretty
                e.getPlayer().sendMessage("Canceled purchase!");
                return;
            }


            boolean result = false;
            try {
                result = checkIfAuctionStillExists(auction).get();
            } catch (InterruptedException | ExecutionException ee) {
                ee.printStackTrace();
            }

            if (!result) {
                e.getPlayer().sendMessage("Item is already sold");
                return;
            }

            removeAuction(auction);
            e.getPlayer().getInventory().addItem(auction.getItem().newInstance().toStack());
            chatBuyPrompt.remove(e.getPlayer());
        }
    }
}
