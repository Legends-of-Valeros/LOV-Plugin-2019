package com.legendsofvaleros.modules.mount;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.mount.api.IMount;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MountAPI extends ListenerModule {
    public interface RPC {
        Promise<List<Mount>> findMounts();

        Promise<PlayerMounts> getPlayerMounts(CharacterId characterId);

        Promise<Object> savePlayerMounts(PlayerMounts pm);

        Promise<Boolean> deletePlayerMounts(CharacterId characterId);
    }

    private RPC rpc;

    private Map<String, Mount> mounts = new HashMap<>();
    private Map<CharacterId, PlayerMounts> playerMounts = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        InterfaceTypeAdapter.register(IMount.class,
                obj -> obj.getId(),
                id -> Promise.make(mounts.get(id)));

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
        });
    }

    public Mount getMount(String mountId) {
        return mounts.get(mountId);
    }

    public Collection<IMount> getMounts(PlayerCharacter pc) {
        return playerMounts.get(pc.getUniqueCharacterId()).mounts;
    }

    /**
     * Adds a mount to a player.
     */
    public void addMount(PlayerCharacter pc, IMount mount) {
        if (mount == null)
            return;

        getMounts(pc).add(mount);
    }

    private Promise onLogin(PlayerCharacter pc) {
        return rpc.getPlayerMounts(pc.getUniqueCharacterId()).onSuccess(val ->
                playerMounts.put(pc.getUniqueCharacterId(), val.orElseGet(() -> new PlayerMounts(pc))));
    }

    private Promise onLogout(CharacterId characterId) {
        return rpc.savePlayerMounts(playerMounts.remove(characterId));
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerMounts(characterId);
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Mounts");

            onLogin(event.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler
        public void onPlayerLeave(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Mounts");

            onLogout(event.getPlayerCharacter().getUniqueCharacterId())
                    .onFailure((Consumer<Throwable>) err -> MessageUtil.sendSevereException(MountAPI.this, event.getPlayer(), err))
                    .on(lock::release);
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent event) {
            onDelete(event.getPlayerCharacter().getUniqueCharacterId())
                    .onFailure((Consumer<Throwable>) err -> MessageUtil.sendSevereException(MountAPI.this, event.getPlayer(), err));
        }
    }
}
