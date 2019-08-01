package com.legendsofvaleros.modules.friends;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;

import java.util.*;

/**
 * Created by Crystall on 07/12/2019
 */
public class FriendsAPI extends ModuleListener {
    public interface RPC {
        Promise<List<UUID>> getAllFriends(UUID uuid);

        Promise<Boolean> saveFriend(UUID player, UUID friend, Date friendsSince);

        Promise<Boolean> deleteFriend(UUID player, UUID friend);
    }

    private FriendsAPI.RPC rpc;
    Map<UUID, List<UUID>> playerFriendsMap = new HashMap<>();

    @Override
    public void onPostLoad() {
        super.onPostLoad();
        this.rpc = APIController.create(FriendsAPI.RPC.class);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public Promise<List<UUID>> onLogin(UUID uuid) {
        return rpc.getAllFriends(uuid);
    }

    public void saveFriend(FriendRequest friendRequest) {
        getScheduler().executeInMyCircle(() -> {
            rpc.saveFriend(friendRequest.getSenderID(), friendRequest.getReceiverID(), new Date()).onSuccess(() -> {
                playerFriendsMap.get(friendRequest.getSenderID()).add(friendRequest.getReceiverID());
                playerFriendsMap.get(friendRequest.getReceiverID()).add(friendRequest.getSenderID());
            }).onFailure(Throwable::printStackTrace);
        });
    }

    public Promise<Boolean> deleteFriend(UUID uuid, UUID friend) {
        return rpc.deleteFriend(uuid, friend);
    }

}
