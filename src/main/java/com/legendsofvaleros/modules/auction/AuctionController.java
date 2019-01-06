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
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.scheduler.InternalTask;
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
@DependsOn(Gear.class)
@DependsOn(Characters.class)
public class AuctionController extends ModuleListener {
    private static AuctionController instance;

    public static AuctionController getInstance() {
        return instance;
    }

    public HashMap<CharacterId, AuctionChatPrompt> auctionPrompts = new HashMap<>();
//    private static final Cache<String, Inventory> cache = CacheBuilder.newBuilder()
//            .concurrencyLevel(4)
//            .maximumSize(1024)
//            .expireAfterAccess(5, TimeUnit.MINUTES)
//            .build();

    private static ORMTable<Auction> auctionsTable;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        auctionsTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), Auction.class);
        NPCs.registerTrait("auctioneer", TraitAuctioneer.class);

        //TODO add scheduler that checks every second if an item has run out and if it is an bid item
        getScheduler().executeInMyCircleTimer(new InternalTask(() -> {

        }), 0, 1000);
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
                        .forEach(auctions::add)
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
     * @param auction
     * @return
     */
    public ListenableFuture<Boolean> checkIfAuctionStillExists(Auction auction) {
        SettableFuture<Boolean> ret = SettableFuture.create();

        if (!ret.isDone()) {
            getScheduler().executeInMyCircle(new InternalTask(() ->
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
                            }).execute(true))
            );
        }

        return ret;
    }

    /**
     * Starts an auction chat prompt to (buy / sell / bid) an item
     * @param p
     * @param itemData
     */
    public void startPrompt(Player p, GearItem.Data itemData, AuctionChatPrompt.AuctionPromptType type) {
        this.startPrompt(p, new Auction(Characters.getPlayerCharacter(p).getUniqueCharacterId(), itemData), type);
    }

    public void startPrompt(Player p, Auction auction, AuctionChatPrompt.AuctionPromptType type) {
        p.closeInventory();
        if (Characters.isPlayerCharacterLoaded(p)) {
            PlayerCharacter playerCharacter = Characters.getPlayerCharacter(p);
            CharacterId characterId = playerCharacter.getUniqueCharacterId();
            if (auctionPrompts.containsKey(characterId)) {
                //TODO check if this goes shorter
                p.sendMessage("You are already " + auctionPrompts.get(characterId).getPrompt().getChatString() + " an item.");
                return;
            }
            auctionPrompts.put(characterId, new AuctionChatPrompt(playerCharacter, auction, type));
        }
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

                auctionPrompts.remove(characterId);
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
     * Returns true if a playerCharacter is in an auctionprompt
     * @param p
     * @return
     */
    public boolean isPrompted(Player p) {
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(p);
        if (playerCharacter.isCurrent()) {
            if (auctionPrompts.containsKey(playerCharacter.getUniqueCharacterId())) {
                return true;
            }
        }
        return false;
    }
}
