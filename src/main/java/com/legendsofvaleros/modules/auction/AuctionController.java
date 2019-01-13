package com.legendsofvaleros.modules.auction;

import com.codingforcookies.doris.orm.ORMTable;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.auction.traits.TraitAuctioneer;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.mailbox.Mail;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.scheduler.InternalTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by Crystall on 10/10/2018
 */
@DependsOn(NPCs.class)
@DependsOn(GearController.class)
@DependsOn(Characters.class)
public class AuctionController extends ModuleListener {
    private static AuctionController instance;
    //auction fee in percentage
    public static final float AUCTION_FEE = 0.1f;

    public static AuctionController getInstance() {
        return instance;
    }

    private HashMap<CharacterId, AuctionChatPrompt> auctionPrompts = new HashMap<>();
//    private static final Cache<String, Inventory> cache = CacheBuilder.newBuilder()
//            .concurrencyLevel(4)
//            .maximumSize(1024)
//            .expireAfterAccess(5, TimeUnit.MINUTES)
//            .build();

    private static ORMTable<Auction> auctionsTable;
    private static ORMTable<BidHistoryEntry> auctionBidHistoryTable;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        auctionsTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), Auction.class);
        auctionBidHistoryTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), BidHistoryEntry.class);
        NPCs.registerTrait("auctioneer", TraitAuctioneer.class);

        getScheduler().executeInMyCircleTimer(new InternalTask(() -> {
            try {
                SettableFuture<ArrayList<Auction>> future = this.loadBidAuctions();

                future.addListener(() -> {
                    try {
                        ArrayList<Auction> auctions = future.get();
                        if(auctions == null) return;

                        auctions.forEach(auction -> {
                            if (auction.getValidUntil() <= System.currentTimeMillis()) {
                                this.handleBidEnd(auction);
                            }
                        });
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }, getScheduler()::async);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }), 0, 15000); //call every 15 seconds.
    }

    public void onUnload() {
        super.onUnload();
    }

    public SettableFuture<ArrayList<Auction>> loadEntries() {
        SettableFuture<ArrayList<Auction>> ret = SettableFuture.create();
        ArrayList<Auction> auctions = new ArrayList<>();

        getScheduler().executeInMyCircle(new InternalTask(() ->
                auctionsTable.query()
                        .all()
                        .forEach((auction, i) -> auctions.add(auction))
                        .onFinished(() -> ret.set(auctions))
                        .execute(false))
        );

        return ret;
    }

    public SettableFuture<ArrayList<Auction>> loadBidAuctions() {
        SettableFuture<ArrayList<Auction>> ret = SettableFuture.create();
        ArrayList<Auction> auctions = new ArrayList<>();

        getScheduler().executeInMyCircle(new InternalTask(() ->
                auctionsTable.query()
                        .select()
                        .where("is_bid_offer", true)
                        .build()
                        .forEach((auction, i) -> auctions.add(auction))
                        .onFinished(() -> ret.set(auctions))
                        .execute(false))
        );

        return ret;
    }

    private void addAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() ->
                auctionsTable.query().insert().values(
                        "owner_id", auction.getOwnerId().toString(),
                        "auction_item", auction.getItem().toString(),
                        "valid_until", auction.getValidUntil(),
                        "is_bid_offer", auction.isBidOffer(),
                        "price", auction.getPrice()
                ).build().execute(true))
        );
    }

    private void removeAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() ->
                auctionsTable.query()
                        .remove()
                        .where("id", auction.getId())
                        .build()
                        .execute(true))
        );
    }

    /**
     * Query for security reason, to make sure, that the item did not get bought by another player
     *
     * @param auction
     * @return
     */
    private ListenableFuture<Boolean> checkIfAuctionStillExists(Auction auction) {
        SettableFuture<Boolean> ret = SettableFuture.create();

        if (!ret.isDone()) {
            getScheduler().executeInMyCircle(new InternalTask(() ->
                    auctionsTable.query()
                            .select()
                            .where("id", auction.getId())
                            .build()
                            .callback((statement, count) -> {
                                if (count == 0) {
                                    ret.set(false);
                                    return;
                                }
                                ret.set(true);
                            }).execute(true))
            );
        }

        return ret;
    }

    public ListenableFuture<ArrayList<BidHistoryEntry>> getBidHistory(Auction auction) {
        SettableFuture<ArrayList<BidHistoryEntry>> ret = SettableFuture.create();
        ArrayList<BidHistoryEntry> entries = new ArrayList<>();

        getScheduler().executeInMyCircle(new InternalTask(() ->
                auctionBidHistoryTable.query()
                        .select()
                        .where("auction_id", auction.getId())
                        .build()
                        .forEach((entry, i) -> entries.add(entry))
                        .onFinished(() -> ret.set(entries))
                        .execute(false))
        );

        return ret;
    }

    /**
     * adds a bid entry to the table
     *
     * @param entry
     */
    public void addBidEntry(BidHistoryEntry entry) {
        getScheduler().executeInMyCircle(new InternalTask(() ->
                auctionBidHistoryTable.query().insert().values(
                        "auction_id", entry.getAuctionId(),
                        "character_id", entry.getCharacterId(),
                        "bid_number", entry.getBidNumber(),
                        "bid_price", entry.getPrice()
                ).build().execute(true))
        );
    }

    /**
     * Starts an auction chat prompt to (buy / sell / bid) an item
     *
     * @param p
     * @param itemData
     */
    public void startPrompt(Player p, Gear.Data itemData, AuctionChatPrompt.AuctionPromptType type) {
        this.startPrompt(p, new Auction(Characters.getPlayerCharacter(p).getUniqueCharacterId(), itemData), type);
    }

    /**
     * Starts an AuctionChatPrompt and guides the user through the process
     *
     * @param p
     * @param auction
     * @param type
     */
    public void startPrompt(Player p, Auction auction, AuctionChatPrompt.AuctionPromptType type) {
        p.closeInventory();
        if (Characters.isPlayerCharacterLoaded(p)) {
            PlayerCharacter playerCharacter = Characters.getPlayerCharacter(p);
            CharacterId characterId = playerCharacter.getUniqueCharacterId();
            if (auctionPrompts.containsKey(characterId)) {
                //TODO make pretty like a princess
                p.sendMessage("You are already " + auctionPrompts.get(characterId).getPrompt().getChatString() + " an item.");
                return;
            }
            auctionPrompts.put(characterId, new AuctionChatPrompt(playerCharacter, auction, type));
        }
    }

    /**
     * Handles the end of an bid auction
     *
     * @param auction
     */
    private void handleBidEnd(Auction auction) {
        BidHistoryEntry entry = auction.getLastBidEntry();
        //TODO make pretty
        Mail mail = new Mail(entry.getCharacterId(), "You won the auction", false);
        mail.setItem(auction.getItem());

        MailboxController.getInstance().saveMail(entry.getCharacterId(), mail);

        removeAuction(auction);
        auction.notifyOwner("Your bid auction has ended. " + ChatColor.BOLD + ChatColor.UNDERLINE + ChatColor.WHITE +
                auction.getItem().toInstance().gear.getName() + ChatColor.RESET + " was sold for " + ChatColor.BOLD + ChatColor.UNDERLINE + ChatColor.WHITE +
                auction.getPriceFormatted(), true
        );

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPromptMessage(AsyncPlayerChatEvent e) {
        if (Characters.isPlayerCharacterLoaded(e.getPlayer())) {
            PlayerCharacter character = Characters.getPlayerCharacter(e.getPlayer());
            if (auctionPrompts.containsKey(character.getUniqueCharacterId())) {
                e.setCancelled(true);
                auctionPrompts.get(character.getUniqueCharacterId()).handleDecision(e.getMessage());
            }
        }
    }

    /**
     * Executes the last step of the AuctionChatPrompt
     *
     * @param characterId
     */
    public void confirmBuyPrompt(CharacterId characterId) {
        if (Characters.isPlayerCharacterLoaded(characterId)) {
            Player p = Characters.getPlayerCharacter(characterId).getPlayer();
            if (auctionPrompts.containsKey(characterId)) {
                AuctionChatPrompt prompt = auctionPrompts.get(characterId);

                boolean result = false;
                try {
                    //TODO add callback, remove freeze of mainthread
                    result = checkIfAuctionStillExists(prompt.getAuction()).get();
                } catch (InterruptedException | ExecutionException ee) {
                    ee.printStackTrace();
                }

                if (!result) {
                    if (Characters.isPlayerCharacterLoaded(characterId)) {
                        p.sendMessage("Item is already sold");
                    }
                    return;
                }

                removeAuction(prompt.getAuction());
                p.getInventory().addItem(prompt.getAuction().getItem().toStack());
            }
        }
    }

    @EventHandler
    public void onLogout(PlayerCharacterLogoutEvent e) {
        if (auctionPrompts.containsKey(e.getPlayerCharacter().getUniqueCharacterId())) {
            auctionPrompts.remove(e.getPlayerCharacter().getUniqueCharacterId());
        }
    }

    public void confirmSellPrompt(CharacterId characterId) {
        if (auctionPrompts.containsKey(characterId)) {
            this.addAuction(auctionPrompts.get(characterId).getAuction());
        }
    }

    public void removePrompt(CharacterId characterId) {
        if (auctionPrompts.containsKey(characterId)) {
            auctionPrompts.remove(characterId);
        }
    }

    /**
     * Returns true if a playerCharacter is in an AuctionChatPrompt
     *
     * @param p
     * @return
     */
    public boolean isPrompted(Player p) {
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(p);
        return playerCharacter.isCurrent() && auctionPrompts.containsKey(playerCharacter.getUniqueCharacterId());
    }
}
