package com.legendsofvaleros.modules.auction;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.scheduler.InternalTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 02/14/2019
 */
public class AuctionAPI extends ModuleListener {

    public interface RPC {
        Promise<List<Auction>> getAllAuctions();

        Promise<List<Auction>> getAllBidAuctions();

        Promise<Auction> getAuction(int id);

        Promise<Boolean> saveAuction(Auction auction);

        Promise<Boolean> deleteAuction(int auctionId);

        Promise<Boolean> saveAuctionBidEntry(BidHistoryEntry entry);

        Promise<List<BidHistoryEntry>> getAllBidHistoryEntries(int auctionId);

    }

    private AuctionAPI.RPC rpc;
    public ArrayList<Auction> auctions = new ArrayList<>();

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        try {
            this.loadEntries().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(AuctionAPI.RPC.class);
    }

    public Promise<List<Auction>> loadEntries() {
        Promise<List<Auction>> promise = rpc.getAllAuctions();
        auctions.clear();

        promise.onSuccess(val -> {
            auctions.addAll(val.orElse(ImmutableList.of()));
        }).onFailure(Throwable::printStackTrace);

        return promise;
    }

    public Promise<List<Auction>> loadBidAuctions() {
        Promise<List<Auction>> promise = rpc.getAllBidAuctions();

        promise.onSuccess(val -> {
            auctions.addAll(val.orElse(ImmutableList.of()));
        }).onFailure(Throwable::printStackTrace);

        return promise;
    }

    public void addAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            auctions.add(auction);
            rpc.saveAuction(auction);
        }));
    }

    public void updateAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveAuction(auction);

        }));
    }

    public void removeAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> rpc.deleteAuction(auction.getId())));
    }

    /**
     * Query for security reason, to make sure, that the item did not get bought by another player
     *
     * @param auction
     * @return
     */
    public boolean checkIfAuctionStillExists(Auction auction) {
        //TODO add multi server support
        if (auctions.contains(auction)) return true;
        return false;
    }

    public ListenableFuture<ArrayList<BidHistoryEntry>> getBidHistory(Auction auction) {
        SettableFuture<ArrayList<BidHistoryEntry>> ret = SettableFuture.create();
        ArrayList<BidHistoryEntry> entries = new ArrayList<>();

        getScheduler().executeInMyCircle(new InternalTask(() ->
                rpc.getAllBidHistoryEntries(auction.getId())
        ));

        return ret;
    }

    /**
     * adds a bid entry to the table
     *
     * @param entry
     */
    public void addBidEntry(BidHistoryEntry entry) {
        getScheduler().executeInMyCircle(new InternalTask(() -> rpc.saveAuctionBidEntry(entry)));
    }

}