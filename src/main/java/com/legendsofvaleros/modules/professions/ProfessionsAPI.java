package com.legendsofvaleros.modules.professions;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.professions.gathering.mining.MiningNode;
import com.legendsofvaleros.modules.professions.gathering.mining.MiningTier;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.scheduler.InternalTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crystall on 02/14/2019
 */
public class ProfessionsAPI extends ModuleListener {

    public interface RPC {
        //mining
        Promise<List<MiningNode>> getAllMiningNodes();

        Promise<List<MiningNode>> getAllMiningNodesByZoneId(String zoneId);

        Promise<Boolean> saveMiningNode(MiningNode miningNode);

        Promise<Boolean> deleteMiningNode(int id);

        //herbalism

        //skinning
    }

    private ProfessionsAPI.RPC rpc;
    public Map<String, List<MiningNode>> zoneMiningNodes = new HashMap<>();
    Map<String, List<MiningNode>> zoneHerbalismnNodes = new HashMap<>();
    Map<String, List<MiningNode>> zoneSkinningNodes = new HashMap<>();


    @Override
    public void onPostLoad() {
        super.onPostLoad();
        this.rpc = APIController.create(ProfessionsAPI.RPC.class);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void loadNodesByZone(Zone zone) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.getAllMiningNodesByZoneId(zone.id).onSuccess(val -> {
                List<MiningNode> nodes = val.orElse(ImmutableList.of());
                zoneMiningNodes.put(zone.id, nodes);
                getLogger().info("[" + zone.name + " - " + zone.subname + "] Loaded " + nodes.size() + " mining nodes");

                //execute in spigot scheduler because of async block remove / place
                getScheduler().executeInSpigotCircle(() -> {
                    for (MiningNode node : nodes) {
                        if (node.getLocation().getBlock().getType() != MiningTier.getTier(node.getTier()).getOreType()) {
                            node.getLocation().getBlock().setType(MiningTier.getTier(node.getTier()).getOreType());
                        }
                    }
                });
            }).onFailure(Throwable::printStackTrace);
        }));
    }

    void saveMiningNode(MiningNode miningNode) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveMiningNode(miningNode).onSuccess(() -> {
                if (zoneMiningNodes.containsKey(miningNode.getZoneId())) {
                    zoneMiningNodes.get(miningNode.getZoneId()).add(miningNode);
                }
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
                    rpc.deleteMiningNode(miningNode.getId()).onSuccess(val -> {
                        zoneMiningNodes.values().remove(miningNode);
                    }).onFailure(Throwable::printStackTrace);
                })
        );
    }
}