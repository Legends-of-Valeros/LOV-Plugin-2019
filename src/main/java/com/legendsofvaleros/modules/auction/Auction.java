package com.legendsofvaleros.modules.auction;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.mailbox.Mail;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Dictionary;

/**
 * Created by Crystall on 10/10/2018
 */
@Table(name = "auctions")
public class Auction {
    @Column(primary = true, autoincrement = true, name = "id")
    private int id;

    @Column(name = "owner_id")
    private CharacterId ownerId;


    @Column(name = "auction_item")
    private GearItem.Data item;

    @Column(name = "price")
    private int price;

    @Column(name = "valid_until")
    private int validUntil;

    @Column(name = "is_bid_offer")
    private boolean isBidOffer = false;

    private Dictionary<String, Integer> bidHistory;

    public Auction(CharacterId ownerId, GearItem.Data item) {
        this.ownerId = ownerId;
        this.item = item;
    }

    public Auction(CharacterId ownerId, int price, GearItem.Data item, boolean isBidOffer) {
        this.ownerId = ownerId;
        this.price = price;
        this.item = item;
        this.isBidOffer = isBidOffer;
    }

    /**
     * Returns if a player owns the auction
     * @param player
     * @return
     */
    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(ownerId);
    }

    /**
     * Returns the lore
     * @return
     */
    public ArrayList<String> getDescription() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Owner: " + Characters.getPlayerCharacter(getOwnerId()).getPlayer().getDisplayName());
        lore.add("Type: " + (isBidOffer ? "Bid" : "Sell"));
        lore.add((isBidOffer ? "Current bid: " : "Price: ") + getPriceFormatted());
        return lore;
    }

    /**
     * Bids on an auction, sets the new price, and adds the bidding person to the bid history
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
            playerCharacter.getPlayer().sendMessage("Your bid price " + Money.Format.format(value) + " is below the current bid price of " + getPriceFormatted());
            return false;
        }
        if (Money.sub(playerCharacter, value)) {
            this.price = value;
            this.bidHistory.put(playerCharacter.getUniqueCharacterId().toString(), value);
            notifyOwner(playerCharacter.getPlayer().getDisplayName() + " bid " + Money.Format.format(value) + " on " + getItem().toInstance().gear.getName(), false);
            return true;
        } else {
            playerCharacter.getPlayer().sendMessage("You don't have enough money to bid on " + getItem().toInstance().gear.getName());
        }
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
            this.price = value;
            this.bidHistory.put(playerCharacter.getUniqueCharacterId().toString(), value);
            notifyOwner(playerCharacter.getPlayer().getDisplayName() + " bought " + getItem().toInstance().gear.getName() + "for " + Money.Format.format(value), false);
            return true;
        }

        playerCharacter.getPlayer().sendMessage("You don't have enough money to buy " + amount + " of" + getItem().toInstance().gear.getName());
        return false;
    }

    /**
     * Notifies the owner of the auction
     * @param contentLines
     */
    public void notifyOwner(ArrayList<String> contentLines, boolean sendMail) {
        Player p = Characters.getPlayerCharacter(ownerId).getPlayer();
        StringBuilder contentBuilder = new StringBuilder();
        contentLines.forEach(contentBuilder::append);

        if (sendMail)
            MailboxController.getInstance().addMail(getOwnerId(), new Mail(getOwnerId(), contentBuilder.toString(), false));

        if (p.isOnline() && Characters.isPlayerCharacterLoaded(p)) {
            // TODO make pretty like a princess
            MessageUtil.sendInfo(p, contentBuilder.toString());
        }
    }

    /**
     * Notifies the owner of the auction
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

    public GearItem.Data getItem() {
        return this.item;
    }

    public int getValidUntil() {
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

    public void setItem(GearItem.Data item) {
        this.item = item;
    }

    public void setValidUntil(int validUntil) {
        this.validUntil = validUntil;
    }

    public void setBidOffer(boolean isBidOffer) {
        this.isBidOffer = isBidOffer;
    }

    public Dictionary<String, Integer> getBidHistory() {
        return bidHistory;
    }
}
