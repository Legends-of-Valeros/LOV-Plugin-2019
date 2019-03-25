package com.legendsofvaleros.modules.professions;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.auction.Auction;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.professions.mining.MiningNode;
import com.legendsofvaleros.scheduler.InternalTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crystall on 02/14/2019
 */
public class ProfessionsAPI extends ModuleListener {

    public interface RPC {
        Promise<List<Auction>> getAllMiningNodes();

        Promise<Auction> getMiningNode(int id);

        Promise<Boolean> saveMiningNode();

        Promise<Boolean> deleteMiningNOde();
    }

    private ProfessionsAPI.RPC rpc;
    public ArrayList<MiningNode> miningNodes = new ArrayList<>();

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

        this.rpc = APIController.create(ProfessionsAPI.RPC.class);
    }

    public Promise<List<Auction>> loadEntries() {
        miningNodes.clear();

        return rpc.getAllMiningNodes().onSuccess(val -> {
            miningNodes.addAll(val.orElse(ImmutableList.of()));

            AuctionController.getInstance().getLogger().info("Loaded " + miningNodes.size() + " miningNodes.");
        }).onFailure(Throwable::printStackTrace);
    }

    void addMiningNode(MiningNode miningNode) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveMiningNode(miningNode).onSuccess(() -> {
                miningNodes.add(miningNode);
            }).onFailure(Throwable::printStackTrace);
        }));
    }

    void updateMiningNode(MiningNode miningNode) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveMiningNode(miningNode).onFailure(Throwable::printStackTrace);
        }));
    }

    void removeMiningNode(MiningNode miningNode) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
                    rpc.deleteAuction(auction.getId()).onSuccess(val -> {
                        miningNodes.remove(auction);
                    }).onFailure(Throwable::printStackTrace);
                })
        );
    }
}