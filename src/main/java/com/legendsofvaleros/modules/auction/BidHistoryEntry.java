package com.legendsofvaleros.modules.auction;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.characters.api.CharacterId;

/**
 * Created by Crystall on 01/07/2019
 */
@Table(name = "auction_bid_history")
public class BidHistoryEntry {
    @Column(primary = true, name = "auction_id")
    private int auctionId;

    @Column(name = "character_id")
    private CharacterId characterId;

    //integer to identify the order of the different bids (could also be ordered by value height)
    @Column(name = "bid_number")
    private int bidNumber;

    @Column(name = "bid_price")
    private int price;

    public BidHistoryEntry(int auctionId, CharacterId characterId, int bidNumber, int price) {
        this.auctionId = auctionId;
        this.characterId = characterId;
        this.bidNumber = bidNumber;
        this.price = price;
    }

    public BidHistoryEntry(int auctionId, CharacterId characterId, int price) {
        this.auctionId = auctionId;
        this.characterId = characterId;
        this.price = price;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getBidNumber() {
        return bidNumber;
    }

    public void setBidNumber(int bidNumber) {
        this.bidNumber = bidNumber;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public void setCharacterId(CharacterId characterId) {
        this.characterId = characterId;
    }
}