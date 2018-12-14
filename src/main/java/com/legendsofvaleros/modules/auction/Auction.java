package com.legendsofvaleros.modules.auction;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.Gear;
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

    @Column(name = "id", primary = true, length = 32)
    private int id;

    @Column(name = "price", length = 32)
    private int price;

    @Column(name = "owner_id", length = 32)
    private CharacterId ownerId;

    @Column(name = "auction_item", length = 255)
    private Gear.Data item;

    @Column(name = "valid_until", length = 32)
    private int validUntil;

    @Column(name = "is_bid_offer", length = 32)
    private boolean isBidOffer = false;

    private Dictionary<String, Integer> bidHistory;

    public Auction(CharacterId ownerId, Gear.Data item) {
        this.ownerId = ownerId;
        this.item = item;
    }

    public Auction(CharacterId ownerId, int price, Gear.Data item, boolean isBidOffer) {
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
        // TODO add useful lore description
//        lore.add(Bukkit.getPlayer(getOwnerId()).getDisplayName());
//        lore.add(Bukkit.getPlayer(getOwnerId()).getDisplayName());
//        lore.add(Bukkit.getPlayer(getOwnerId()).getDisplayName());
//        lore.add(Bukkit.getPlayer(getOwnerId()).getDisplayName());
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
            //TODO make pretty
            playerCharacter.getPlayer().sendMessage("Your bid price " + value + "is below the current bid price of " + price);
            return false;
        }
        if (Money.sub(playerCharacter, value)) {
            this.price = value;
            this.bidHistory.put(playerCharacter.getUniqueCharacterId().toString(), value);
            notifyOwner(playerCharacter.getPlayer().getDisplayName() + " bid " + value + " on " + getItem().toInstance().gear.getName(), false);
            return true;
        }
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

    public CharacterId getOwnerId() {
        return this.ownerId;
    }

    public Gear.Data getItem() {
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

    public void setItem(Gear.Data item) {
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
