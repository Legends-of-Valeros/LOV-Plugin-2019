package com.legendsofvaleros.modules.professions;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.professions.gathering.GatheringNode;
import com.legendsofvaleros.modules.professions.gathering.mining.MiningTier;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.scheduler.InternalTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crystall on 02/14/2019
 */
public class ProfessionsAPI extends ListenerModule {
    public interface RPC {
        Promise<List<GatheringNode>> getAllGatheringNodesByZoneId(String zoneId);

        Promise<Boolean> saveGatheringNode(GatheringNode gatheringNode);

        Promise<Boolean> deleteGatheringNode(int id);

        //Playerprofessions
        Promise<Boolean> savePlayerProfessions(PlayerProfession playerProfession);

        Promise<Boolean> deletePlayerProfessions(CharacterId playerProfessions);

        Promise<PlayerProfession> getPlayerProfessions(CharacterId characterId);

    }

    private RPC rpc;
    public Map<String, List<GatheringNode>> zoneGatheringNodes = new HashMap<>();
    private final Map<CharacterId, PlayerProfession> playerProfessionsMap = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();
        this.rpc = APIController.create(RPC.class);
    }

    /**
     * @param zone
     */
    public void loadNodesByZone(Zone zone) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.getAllGatheringNodesByZoneId(zone.id).onSuccess(val -> {
                List<GatheringNode> nodes = val.orElse(ImmutableList.of());
                zoneGatheringNodes.put(zone.id, nodes);

                //TODO specify which node got loaded?
                getLogger().info("[" + zone.name + " - " + zone.subname + "] Loaded " + nodes.size() + " gathering nodes");

                //execute in spigot scheduler because of async block remove / place
                getScheduler().executeInSpigotCircle(() -> {
                    for (GatheringNode node : nodes) {
                        if (node.getLocation().getBlock().getType() != node.getNodeMaterial()) {
                            node.getLocation().getBlock().setType(MiningTier.getTier(node.getTier()).getOreType());
                        }
                    }
                });
            }).onFailure(Throwable::printStackTrace);
        }));
    }

    /**
     * @param gatheringNode
     */
    public void saveGatheringNode(GatheringNode gatheringNode) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveGatheringNode(gatheringNode).onSuccess(() -> {
                if (zoneGatheringNodes.containsKey(gatheringNode.getZoneId())) {
                    zoneGatheringNodes.get(gatheringNode.getZoneId()).add(gatheringNode);
                }
            }).onFailure(Throwable::printStackTrace);
        }));
    }

    /**
     * @param gatheringNode
     */
    public void updateGatheringNode(GatheringNode gatheringNode) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
            rpc.saveGatheringNode(gatheringNode).onFailure(Throwable::printStackTrace);
        }));
    }

    /**
     * @param gatheringNode
     */
    public void removeGatheringNode(GatheringNode gatheringNode) {
        getScheduler().executeInMyCircle(new InternalTask(() -> {
                    rpc.deleteGatheringNode(gatheringNode.getId()).onSuccess(val -> {
                        zoneGatheringNodes.values().remove(gatheringNode);
                    }).onFailure(Throwable::printStackTrace);
                })
        );
    }

    /////////////////////////////////////////////////////////////
    //                 PlayerProfession                        //
    /////////////////////////////////////////////////////////////

    /**
     * @param characterId
     * @return
     */
    public Promise<PlayerProfession> onLogin(CharacterId characterId) {
        Promise<PlayerProfession> promise = rpc.getPlayerProfessions(characterId);

        promise.onSuccess(playerProfessions -> {
            playerProfessionsMap.put(characterId, playerProfessions.orElse(
                    new PlayerProfession(characterId, 0, 0, 0, 0))
            );
        });

        return promise;
    }

    /**
     * @param characterId
     * @return
     */
    public Promise<Boolean> onLogout(CharacterId characterId) {
        PlayerProfession playerProfession = playerProfessionsMap.remove(characterId);

        if (playerProfession == null) {
            return Promise.make(false);
        }

        return rpc.savePlayerProfessions(playerProfession);
    }

    /**
     * @param characterId
     * @return
     */
    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerProfessions(characterId);
    }
}
