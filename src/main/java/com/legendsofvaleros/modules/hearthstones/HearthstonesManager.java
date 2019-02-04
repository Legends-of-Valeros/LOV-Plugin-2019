package com.legendsofvaleros.modules.hearthstones;

import com.codingforcookies.doris.orm.ORMTable;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hearthstone data about players.
 * <p>
 * Loads data from the database on login, writes to the database as changes are made.
 */
public class HearthstonesManager {
    private static ORMTable<HomePoint> homeTable;

    private static Map<CharacterId, HomePoint> homes = new ConcurrentHashMap<>();

    private static long cooldownDuration;

    public static void onEnable() {
        cooldownDuration = HearthstoneController.getInstance().getConfig().getLong("cooldown-seconds") * 1000;

        homeTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), HomePoint.class);

        HearthstoneController.getInstance().registerEvents(new PlayerListener());
    }

    public static HomePoint getHome(PlayerCharacter pc) {
        return homes.get(pc.getUniqueCharacterId());
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
    public static void setHome(final PlayerCharacter pc, final String innName, final Location loc) {
        if (innName == null || loc == null) {
            homes.remove(pc.getUniqueCharacterId());

            homeTable.query()
                    .remove(pc.getUniqueCharacterId().toString())
                    .execute(true);
            return;
        }

        HomePoint home;

        homes.put(pc.getUniqueCharacterId(), (home = new HomePoint(pc.getUniqueCharacterId(), innName, loc)));

        homeTable.save(home, true);
    }

    /**
     * Gets the timestamp of the earliest time a player can use their hearthstone again after the last
     * time.
     * @param pc The name of the player whose cooldown expiration to get.
     * @return The earliest time the player can use their hearthstone again. <code>0</code> if no
     * cooldown as found in the locally tracked data for a player with the given name.
     */
    public static long getCooldown(PlayerCharacter pc) {
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
    public static void addCooldown(final PlayerCharacter pc) {
        pc.getCooldowns().overwriteCooldown("hearthstone",
                Cooldowns.CooldownType.CALENDAR_TIME,
                System.currentTimeMillis() + cooldownDuration);
    }

    private static void loadPlayer(final PlayerCharacter pc, PhaseLock lock) {
        homeTable.query()
                .get(pc.getUniqueCharacterId().toString())
                .forEach((home, i) -> {
                    World world = Bukkit.getWorld(home.world);

                    if (world == null) {
                        HearthstoneController.getInstance().getLogger().warning(
                                "Player '" + pc.getUniqueCharacterId() + "' has a home in the world '" + home.world
                                        + "', but that world was not found on this server. "
                                        + "Their home was not successfully loaded as a result.");
                        return;
                    }

                    homes.put(pc.getUniqueCharacterId(), home);
                })
                .onFinished(lock::release)
                .execute(true);
    }

    /**
     * Listens for player logins and logouts.
     * <p>
     * Triggers reads from the database on login, and cleanup on logout.
     */
    private static class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerCharacterDelete(final PlayerCharacterRemoveEvent event) {
            setHome(event.getPlayerCharacter(), null, null);
        }

        @EventHandler
        public void onPlayerCharacterLoad(final PlayerCharacterStartLoadingEvent event) {
            loadPlayer(event.getPlayerCharacter(), event.getLock("Hearthstone"));
        }

        @EventHandler
        public void onPlayerCharacterQuit(PlayerCharacterLogoutEvent event) {
            homes.remove(event.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}
