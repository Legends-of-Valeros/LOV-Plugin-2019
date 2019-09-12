package com.legendsofvaleros.modules.auction;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.scheduler.InternalTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 02/14/2019
 */
public class AuctionAPI extends ListenerModule {

    public interface RPC {
        Promise<List<Auction>> getAllAuctions();

        Promise<List<Auction>> getAllBidAuctions();

        Promise<Auction> getAuction(int id);

        Promise<Object> saveAuction(Auction auction);

        Promise<Boolean> deleteAuction(int auctionId);

        Promise<Object> saveAuctionBidEntry(BidHistoryEntry entry);

        Promise<List<BidHistoryEntry>> getAllBidHistoryEntries(int auctionId);
    }

    private AuctionAPI.RPC rpc;
    public ArrayList<Auction> auctions = new ArrayList<>();

    @Override
    public void onPostLoad() {
        super.onPostLoad();
        this.rpc = APIController.create(AuctionAPI.RPC.class);

        this.loadEntries().get();
    }

    @Override
    public void onLoad() {
        super.onLoad();

    }

    public Promise<List<Auction>> loadEntries() {
        auctions.clear();

        return rpc.getAllAuctions().onSuccess(val -> {
            auctions.addAll(val.orElse(ImmutableList.of()));

            getLogger().info("Loaded " + auctions.size() + " auctions.");
        });
    }

    public Promise<List<Auction>> loadBidAuctions() {
        Promise<List<Auction>> promise = rpc.getAllBidAuctions();

        promise.onSuccess(val -> {
            auctions.addAll(val.orElse(ImmutableList.of()));
        });

        return promise;
    }

    void addAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveAuction(auction).onSuccess(() -> {
                auctions.add(auction);
            });
        }));
    }

    void updateAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveAuction(auction);
        }));
    }

    void removeAuction(Auction auction) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
                    rpc.deleteAuction(auction.getId()).onSuccess(val -> {
                        auctions.remove(auction);
                    });
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
                    //TODO add callback / listener
                    result[0] = rpc.getAuction(auction.getId()).get();
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