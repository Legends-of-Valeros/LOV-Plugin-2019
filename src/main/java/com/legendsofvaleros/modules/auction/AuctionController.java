package com.legendsofvaleros.modules.auction;

import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.mailbox.Mail;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Crystall on 10/10/2018
 */
@DependsOn(GearController.class)
@DependsOn(Characters.class)
@ModuleInfo(name = "Auctions", info = "")
public class AuctionController extends AuctionAPI {
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
    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        getScheduler().executeInMyCircleTimer(new InternalTask(() -> {
            auctions.forEach(auction -> {
                if (auction.isBidOffer()) {
                    if (auction.getValidUntil() < TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) {
                        this.handleBidEnd(auction);
                    }
                }
            });
        }), 0, 1000); //call every 15 seconds.
    }

    public void onUnload() {
        super.onUnload();
    }

    /**
     * Starts an auction chat prompt to (buy / sell / bid) an item
     * @param p
     * @param itemData
     */
    public void startPrompt(Player p, Gear.Data itemData, AuctionChatPrompt.AuctionPromptType type) {
        this.startPrompt(p, new Auction(Characters.getPlayerCharacter(p).getUniqueCharacterId(), p.getDisplayName(), itemData), type);
    }

    /**
     * Starts an AuctionChatPrompt and guides the user through the process
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
                MessageUtil.sendError(p, "You are already " + auctionPrompts.get(characterId).getPrompt().getChatString() + " an item.");
                return;
            }
            auctionPrompts.put(characterId, new AuctionChatPrompt(playerCharacter, auction, type));
        }
    }

    /**
     * Handles the end of an bid auction
     * @param auction
     */
    private void handleBidEnd(Auction auction) {
        Mail mail = new Mail(auction.getHighestBidderId(), null, "AuctionHouse: You won the auction", "Congratulations, you won the auction", false);
        mail.setItem(auction.getItem());
        MailboxController.getInstance().saveMailToMailbox(auction.getHighestBidderId(), mail);

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
     * @param characterId
     */
    public void confirmBuyPrompt(CharacterId characterId, int amount) {
        if (Characters.isPlayerCharacterLoaded(characterId)) {
            Player p = Characters.getPlayerCharacter(characterId).getPlayer();
            if (auctionPrompts.containsKey(characterId)) {
                AuctionChatPrompt prompt = auctionPrompts.get(characterId);

                ItemStack is = prompt.getAuction().getItem().toStack();
                is.setAmount(amount);
                p.getInventory().addItem(is);

                if (amount < prompt.getAuction().getItem().amount) {
                    Gear.Data data = prompt.getAuction().getItem();
                    data.amount = data.amount - amount;
                    prompt.getAuction().setItem(data);
                    updateAuction(prompt.getAuction());
                    return;
                }

                removeAuction(prompt.getAuction());
            }
        }
    }

    /**
     * Executes the last step of the AuctionChatPrompt
     * @param characterId
     */
    public void confirmBidPrompt(CharacterId characterId) {
        if (Characters.isPlayerCharacterLoaded(characterId)) {
            if (auctionPrompts.containsKey(characterId)) {
                AuctionChatPrompt prompt = auctionPrompts.get(characterId);
                updateAuction(prompt.getAuction());
            }
        }
    }

    @EventHandler
    public void onLogout(PlayerCharacterLogoutEvent e) {
        auctionPrompts.remove(e.getPlayerCharacter().getUniqueCharacterId());
    }

    public void confirmSellPrompt(CharacterId characterId) {
        if (auctionPrompts.containsKey(characterId)) {
            this.addAuction(auctionPrompts.get(characterId).getAuction());
        }
    }

    public void removePrompt(CharacterId characterId) {
        auctionPrompts.remove(characterId);
    }

    /**
     * Returns true if a playerCharacter is in an AuctionChatPrompt
     * @param p
     * @return
     */
    public boolean isPrompted(Player p) {
        if (!Characters.isPlayerCharacterLoaded(p)) return false;

        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(p);
        return playerCharacter.isCurrent() && auctionPrompts.containsKey(playerCharacter.getUniqueCharacterId());
    }
}
