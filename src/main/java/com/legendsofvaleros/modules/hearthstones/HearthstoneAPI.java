package com.legendsofvaleros.modules.hearthstones;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.hearthstones.core.HomePoint;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HearthstoneAPI {
    public interface RPC {
        Promise<HomePoint> getPlayerHearthstone(CharacterId characterId);
        Promise<Boolean> savePlayerHearthstone(HomePoint point);
        Promise<Boolean> deletePlayerHearthstone(HomePoint point);
    }

    private final RPC rpc;

    private Cache<CharacterId, HomePoint> homes = CacheBuilder.newBuilder()
                                                        .concurrencyLevel(4).build();

    private long cooldownDuration;

    public HearthstoneAPI() {
        this.rpc = APIController.create(HearthstoneController.getInstance(), RPC.class);

        this.cooldownDuration = HearthstoneController.getInstance().getConfig().getLong("cooldown-seconds") * 1000;

        HearthstoneController.getInstance().registerEvents(new PlayerListener());
    }

    public HomePoint getHome(PlayerCharacter pc) {
        return homes.getIfPresent(pc.getUniqueCharacterId());
    }

    /**
     * Sets the home of a player.
     * <p>
     * If the player whose home is being set is offline, this may not take noticeable effect until the
     * next time they log into a server using the same data set.
     * @param pc      The name of the player whose home to set.
     * @param innName The name of the inn.
     * @param loc     The location of the home.
     */
    public Promise<Boolean> setHome(PlayerCharacter pc, String innName, Location loc) {
        HomePoint home = new HomePoint(pc.getUniqueCharacterId(), innName, loc);

        homes.put(pc.getUniqueCharacterId(), home);

        return rpc.savePlayerHearthstone(home);
    }

    public Promise<Boolean> removeHome(PlayerCharacter pc) {
        HomePoint point = homes.getIfPresent(pc.getUniqueCharacterId());

        if(point == null) {
            Promise<Boolean> promise = new Promise<>();
            promise.reject(new IllegalStateException("That player does not have a home set."));
            return promise;
        }

        return rpc.deletePlayerHearthstone(point);
    }

    /**
     * Gets the timestamp of the earliest time a player can use their hearthstone again after the last
     * time.
     * @param pc The name of the player whose cooldown expiration to get.
     * @return The earliest time the player can use their hearthstone again. <code>0</code> if no
     * cooldown as found in the locally tracked data for a player with the given name.
     */
    public long getCooldown(PlayerCharacter pc) {
        if (!pc.getCooldowns().hasCooldown("hearthstone")) return 0;
        return pc.getCooldowns().getCooldown("hearthstone").getRemainingDurationMillis();
    }

    /**
     * Adds a cooldown of the configured length to a player that will stop them from using their
     * hearthstone until it expires.
     * <p>
     * If the player to whom a cooldown is being added is offline, this may not take noticeable effect
     * until the next time they log into a server using the same data set.
     * @param pc The name of the player to add a cooldown to.
     */
    public void addCooldown(final PlayerCharacter pc) {
        pc.getCooldowns().overwriteCooldown("hearthstone",
                Cooldowns.CooldownType.CALENDAR_TIME,
                System.currentTimeMillis() + cooldownDuration);
    }

    private Promise<HomePoint> onLogin(PlayerCharacter pc) {
        return this.rpc.getPlayerHearthstone(pc.getUniqueCharacterId()).onSuccess(val -> {
            if(val != null)
                homes.put(pc.getUniqueCharacterId(), val);
        });
        /*
        HearthstoneController.getInstance().getLogger().warning(
                "Player '" + pc.getUniqueCharacterId() + "' has a home in the world '" + home.world
                        + "', but that world was not found on this server. "
                        + "Their home was not successfully loaded as a result.");*/
    }

    /**
     * Listens for player logins and logouts.
     * <p>
     * Triggers reads from the database on login, and cleanup on logout.
     */
    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerCharacterLoad(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Hearthstone");

            onLogin(event.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler
        public void onPlayerCharacterQuit(PlayerCharacterLogoutEvent event) {
            homes.invalidate(event.getPlayerCharacter().getUniqueCharacterId());
        }

        @EventHandler
        public void onPlayerCharacterDelete(PlayerCharacterRemoveEvent event) {
            removeHome(event.getPlayerCharacter());
        }
    }
}
