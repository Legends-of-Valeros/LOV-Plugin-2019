package com.legendsofvaleros.modules.auction;

import com.legendsofvaleros.modules.bank.core.Money;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Crystall on 10/10/2018
 */
public class Auction {
    private int id;

    private CharacterId ownerId;

    //to support offline players and multiple server
    private String ownerName;

    private Gear.Data item;

    private int price;

    private long validUntil;

    private boolean isBidOffer = false;

    private CharacterId highestBidderId;

    //to support offline players and multiple server
    private String highestBidderName;

    public Auction(CharacterId ownerId, String ownerName, Gear.Data item) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.item = item;
    }

    public Auction(CharacterId ownerId, String ownerName, int price, Gear.Data item, boolean isBidOffer) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.price = price;
        this.item = item;
        this.isBidOffer = isBidOffer;
    }

    /**
     * Returns if a player owns the auction
     *
     * @param player
     * @return
     */
    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(ownerId.getPlayerId());
    }

    /**
     * Returns the lore
     *
     * @return
     */
    public ArrayList<String> getDescription() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Owner: " + getOwnerName());
        lore.add("Type: " + (isBidOffer ? "Bid" : "Sell"));
        if (isBidOffer) {
            lore.add("Highest Bidder: " + highestBidderName);
        }
        lore.add((isBidOffer ? "Current bid: " : "Price: ") + getPriceFormatted());
        return lore;
    }

    /**
     * Bids on an auction, sets the new price, and adds the bidding person to the bid history
     *
     * @param value
     * @return
     */
    public boolean bid(CharacterId characterId, int value) {
        if (!isBidOffer) {
            return false;
        }
        if (!Characters.isPlayerCharacterLoaded(characterId)) {
            return false;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(characterId);
        if (value <= price) {
            playerCharacter.getPlayer().sendMessage("Your bid price " + Money.Format.format(value) + " is not higher than the current bid price of " + getPriceFormatted());
            return false;
        }
        if (Money.sub(playerCharacter, value)) {
            //re-add the money to the previous bidder - auction fee
            PlayerCharacter previousBidder = Characters.getPlayerCharacter(highestBidderId);

            //set the values of the current bidder and also save
            this.price = value;
            this.highestBidderId = playerCharacter.getUniqueCharacterId();
            this.highestBidderName = playerCharacter.getPlayer().getDisplayName();
            BidHistoryEntry entry = new BidHistoryEntry(this.id, playerCharacter.getUniqueCharacterId(), this.price);
            AuctionController.getInstance().addBidEntry(entry);
            notifyOwner(playerCharacter.getPlayer().getDisplayName() + " bid " + Money.Format.format(value) + " on " + getItem().toInstance().gear.getName(), false);
            return true;
        }
        playerCharacter.getPlayer().sendMessage("You don't have enough money to bid on " + getItem().toInstance().gear.getName());
        return false;
    }

    public boolean buy(CharacterId characterId, int amount) {
        if (isBidOffer) {
            return false;
        }
        if (!Characters.isPlayerCharacterLoaded(characterId)) {
            return false;
        }
        int value = price * amount;
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(characterId);

        if (Money.sub(playerCharacter, value)) {
            notifyOwner(playerCharacter.getPlayer().getDisplayName() + " bought " + getItem().toInstance().gear.getName() + " for " + Money.Format.format(value), false);
            return true;
        }

        playerCharacter.getPlayer().sendMessage("You don't have enough money to buy " + amount + " of" + getItem().toInstance().gear.getName());
        return false;
    }

    /**
     * Notifies the owner of the auction
     *
     * @param contentLines
     */
    public void notifyOwner(ArrayList<String> contentLines, boolean sendMail) {
        Player p = Characters.getPlayerCharacter(ownerId).getPlayer();
        StringBuilder contentBuilder = new StringBuilder();
        contentLines.forEach(contentBuilder::append);

        if (sendMail) {
            return;
        }

        if (Characters.isPlayerCharacterLoaded(p)) {
            // TODO make pretty like a princess
            MessageUtil.sendInfo(p, contentBuilder.toString());
        }
    }

    /**
     * Notifies the owner of the auction
     *
     * @param message
     */
    public void notifyOwner(String message, boolean sendMail) {
        ArrayList<String> messages = new ArrayList<>();
        messages.add(message);
        notifyOwner(messages, sendMail);
    }

    public int getId() {
        return this.id;
    }

    public int getPrice() {
        return this.price;
    }

    public String getPriceFormatted() {
        return Money.Format.format(this.price);
    }

    public CharacterId getOwnerId() {
        return this.ownerId;
    }

    public Gear.Data getItem() {
        return this.item;
    }

    public long getValidUntil() {
        return this.validUntil;
    }

    public boolean isBidOffer() {
        return this.isBidOffer;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setOwnerId(CharacterId ownerId) {
        this.ownerId = ownerId;
    }

    public void setItem(Gear.Data item) {
        this.item = item;
    }

    public void setValidUntil(long validUntil) {
        this.validUntil = validUntil;
    }

    public void setBidOffer(boolean isBidOffer) {
        this.isBidOffer = isBidOffer;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public ArrayList<BidHistoryEntry> getBidHistory() {
        //TODO prevent freeze of thread with callback
        ArrayList<BidHistoryEntry> entries = new ArrayList<>();
        try {
            entries = AuctionController.getInstance().getBidHistory(this).get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entries;
    }

    public BidHistoryEntry getLastBidEntry() {
        ArrayList<BidHistoryEntry> entries = getBidHistory();
        return entries.get(entries.size() - 1);
    }
}
