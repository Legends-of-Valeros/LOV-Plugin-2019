package com.legendsofvaleros.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerData {
    private interface RPC {
        Promise<PlayerData> getPlayer(UUID uuid);

        Promise<Boolean> savePlayer(PlayerData data);
    }

    private static RPC rpc;

    public static final Cache<UUID, PlayerData> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    static void onEnable() {
        rpc = APIController.create(RPC.class);
    }

    public static Promise<PlayerData> get(UUID uuid) {
        return rpc.getPlayer(uuid).onSuccess(player -> {
            if (player.isPresent()) {
                String currentName = Bukkit.getOfflinePlayer(uuid).getName();
                if (currentName != null && ! currentName.equals(player.get().username)) {
                    player.get().username = currentName;
                }
            }
        });
    }

    public static Promise<Boolean> save(PlayerData data) {
        return rpc.savePlayer(data);
    }

    public final UUID uuid;
    public String username;
    public String resourcePack;
    public boolean resourcePackForced;

    PlayerData(UUID uuid) {
        this.uuid = uuid;
    }
}