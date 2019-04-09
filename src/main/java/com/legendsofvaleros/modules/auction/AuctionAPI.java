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
        auctions.clear();

        return rpc.getAllAuctions().onSuccess(val -> {
            auctions.addAll(val.orElse(ImmutableList.of()));

            AuctionController.getInstance().getLogger().info("Loaded " + auctions.size() + " auctions.");
        }).onFailure(Throwable::printStackTrace);
    }

    public Promise<List<Auction>> loadBidAuctions() {
        Promise<List<Auction>> promise = rpc.getAllBidAuctions();

        promise.onSuccess(val -> {
            auctions.addAll(val.orElse(ImmutableList.of()));
        }).onFailure(Throwable::printStackTrace);

        return promise;
    }

    void addAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveAuction(auction).onSuccess(() -> {
                auctions.add(auction);
            }).onFailure(Throwable::printStackTrace);
        }));
    }

    void updateAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveAuction(auction).onFailure(Throwable::printStackTrace);
        }));
    }

    void removeAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
                    rpc.deleteAuction(auction.getId()).onSuccess(val -> {
                        auctions.remove(auction);
                    }).onFailure(Throwable::printStackTrace);
                })
        );
    }

    /**
     * Query for security reason, to make sure, that the item did not get bought by another player
     * @param auction
     * @return
     */
    boolean checkIfAuctionStillExists(Auction auction) {
        Auction[] result = {null};
        getScheduler().executeInMyCircle(new InternalTask(() -> {
                    try {
                        //TODO add callback / listener
                        result[0] = rpc.getAuction(auction.getId()).get();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
        );
        return result[0] == null;
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
     * @param entry
     */
    public void addBidEntry(BidHistoryEntry entry) {
        getScheduler().executeInMyCircle(new InternalTask(() -> rpc.saveAuctionBidEntry(entry)));
    }

}