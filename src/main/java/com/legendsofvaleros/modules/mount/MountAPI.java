package com.legendsofvaleros.modules.mount;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MountAPI {
    public interface RPC {
        Promise<Mount[]> findMounts();

        Promise<Collection<String>> getPlayerMounts(CharacterId characterId);
        Promise<Boolean> savePlayerMounts(CharacterId characterId, Collection<String> strings);
        Promise<Boolean> deletePlayerMounts(CharacterId characterId);
    }

    private final RPC rpc;

    private static Map<String, Mount> mounts = new HashMap<>();
    private static Multimap<CharacterId, String> playerMounts = HashMultimap.create();

    public MountAPI() {
        this.rpc = APIController.create(MountsController.getInstance(), RPC.class);

        MountsController.getInstance().registerEvents(new PlayerListener());
    }

    public Promise<Mount[]> loadAll() {
        return rpc.findMounts().onSuccess(val -> {
            mounts.clear();

            for(Mount mount : val)
                mounts.put(mount.getId(), mount);

            MountsController.getInstance().getLogger().info("Loaded " + mounts.size() + " mounts.");
        }).onFailure(Throwable::printStackTrace);
    }

    public Mount getMount(String mountId) {
        return mounts.get(mountId);
    }
    public Collection<Mount> getMounts(PlayerCharacter pc) {
        return playerMounts.get(pc.getUniqueCharacterId()).stream()
                .map(mountId -> mounts.get(mountId))
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    /**
     * Adds a mount to a player.
     */
    public void addMount(CharacterId identifier, Mount mount) {
        if (mount == null)
            return;

        playerMounts.put(identifier, mount.getId());
    }

    private Promise<Collection<String>> onLogin(CharacterId characterId) {
        Promise<Collection<String>> promise = rpc.getPlayerMounts(characterId);

        promise.onSuccess(val ->
                val.stream().forEach(mountId ->
                        playerMounts.put(characterId, mountId)));

        return promise;
    }

    private Promise<Boolean> onLogout(CharacterId characterId) {
        Promise<Boolean> promise = rpc.savePlayerMounts(characterId, playerMounts.get(characterId));

        promise.on(() -> playerMounts.removeAll(characterId));

        return promise;
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Mounts");

            onLogin(event.getPlayerCharacter().getUniqueCharacterId()).on(lock::release);
        }

        @EventHandler
        public void onPlayerLeave(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Mounts");

            onLogout(event.getPlayerCharacter().getUniqueCharacterId()).on(lock::release);
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent event) {
            rpc.deletePlayerMounts(event.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}
