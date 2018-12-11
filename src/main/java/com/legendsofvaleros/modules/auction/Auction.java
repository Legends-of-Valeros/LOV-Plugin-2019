package com.legendsofvaleros.modules.auction;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.mailbox.Mail;
import com.legendsofvaleros.modules.mailbox.Mailbox;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

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
    private UUID ownerId;

    @Column(name = "auction_item", length = 255)
    private GearItem item;

    @Column(name = "valid_until", length = 32)
    private int validUntil;

    @Column(name = "is_bid_offer", length = 32)
    private boolean isBidOffer = false;

    public Auction(UUID ownerId, int price, GearItem item) {
        this.ownerId = ownerId;
        this.price = price;
        this.item = item;
        this.validUntil = (int) (System.currentTimeMillis() / 1000D);
    }

    public Auction(UUID ownerId, int price, GearItem item, boolean isBidOffer) {
        this.ownerId = ownerId;
        this.price = price;
        this.item = item;
        this.validUntil = (int) (System.currentTimeMillis() / 1000D);
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
        lore.add(Bukkit.getPlayer(getOwnerId()).getDisplayName());
        lore.add(Bukkit.getPlayer(getOwnerId()).getDisplayName());
        lore.add(Bukkit.getPlayer(getOwnerId()).getDisplayName());
        lore.add(Bukkit.getPlayer(getOwnerId()).getDisplayName());
        return lore;
    }

    /**
     * Notifies the owner of the auction
     * @param contentLines
     */
    public void notifyOwner(ArrayList<String> contentLines) {
        Player p = Bukkit.getPlayer(ownerId);
        CharacterId characterId = Characters.getPlayerCharacter(p).getUniqueCharacterId();
        StringBuilder contentBuilder = new StringBuilder();
        contentLines.forEach(contentBuilder::append);

        if (p.isOnline() && Characters.isPlayerCharacterLoaded(p)) {
            Mailbox mailbox = MailboxController.getInstance().getMailbox(characterId);
            // TODO make pretty like a princess
            p.sendMessage(contentBuilder.toString());
        } else {
            MailboxController.getInstance().addMail(characterId, new Mail(characterId, contentBuilder.toString(), false));
        }
    }

    public int getId() {
        return this.id;
    }

    public int getPrice() {
        return this.price;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public GearItem getItem() {
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

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void setItem(GearItem item) {
        this.item = item;
    }

    public void setValidUntil(int validUntil) {
        this.validUntil = validUntil;
    }

    public void setBidOffer(boolean isBidOffer) {
        this.isBidOffer = isBidOffer;
    }
}
