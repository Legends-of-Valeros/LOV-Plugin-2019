package com.legendsofvaleros.modules.mount;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MountAPI extends ModuleListener {
    public interface RPC {
        Promise<List<Mount>> findMounts();

        Promise<List<String>> getPlayerMounts(CharacterId characterId);
        Promise<Boolean> savePlayerMounts(CharacterId characterId, Collection<String> strings);
        Promise<Boolean> deletePlayerMounts(CharacterId characterId);
    }

    private RPC rpc;

    private Map<String, Mount> mounts = new HashMap<>();
    private Multimap<CharacterId, String> playerMounts = HashMultimap.create();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        registerEvents(new PlayerListener());
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        try {
            this.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public Promise<List<Mount>> loadAll() {
        return rpc.findMounts().onSuccess(val -> {
            mounts.clear();

            val.orElse(ImmutableList.of()).stream().forEach(mount ->
                    mounts.put(mount.getId(), mount));

            getLogger().info("Loaded " + mounts.size() + " mounts.");
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

    private Promise<List<String>> onLogin(CharacterId characterId) {
        return rpc.getPlayerMounts(characterId).onSuccess(val -> {
            val.orElse(ImmutableList.of()).stream().forEach(mount ->
                    playerMounts.put(characterId, mount));
        });
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
