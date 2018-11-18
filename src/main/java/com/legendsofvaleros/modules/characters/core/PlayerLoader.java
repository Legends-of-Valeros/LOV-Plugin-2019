package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.codingforcookies.robert.core.StringUtil;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.events.*;
import com.legendsofvaleros.modules.characters.loading.Callback;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.loading.PlayerLock;
import com.legendsofvaleros.modules.characters.loading.TaskPhase;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.combatengine.api.UnsafePlayerInitializer;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.ui.CharacterCreationListener;
import com.legendsofvaleros.modules.characters.ui.CharacterSelectionListener;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.*;
import java.util.logging.Logger;

/**
 * Defines the logic and schedule used to load player-character data and manage processing players
 * immediately after logging in and immediately after selecting a character.
 * <p>
 * What happens on login:
 * <ol>
 * <li>The player is locked in place and their screen is blocked.
 * <li>The player's base character data is loaded for each of their characters.
 * <li>If the player has no characters, a new one is created for them automatically, they are
 * unlocked, and they enter the tutorial (END). Else if they have characters, they are prompted to
 * pick which character they want to play or if they want to create a new one.
 * <li>If the player selects creating a new character, they are unlocked and they enter character
 * creation (END). Else if they select an existing character, detailed data about that character is
 * loaded.
 * <li>When the detailed data is done loading and the player-character is initialized, the player is
 * unlocked and teleported to their character's last location.
 * </ol>
 */
public class PlayerLoader implements CharacterSelectionListener, Listener {

    // TODO make special case for players logging in dead? Force respawn after they have loaded?

    private static final long SELECT_DELAY = 2L;

    private UnsafePlayerInitializer combatEngine;
    private CombatStatusTracker combatTracker;

    private Map<UUID, PlayerLock> locks;
    private Set<UUID> haveAlreadyLoaded;
    private Set<UUID> firstLogin;

    private Callback<CharacterId> loadingCallback = (value, error) -> {
        if (error != null) {
            LegendsOfValeros.getInstance().getLogger().severe("error while loading a player's character data");
            MessageUtil.sendException(LegendsOfValeros.getInstance(), null, error, true);
        } else if (value != null) {
            Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> onDoneLoading(value));
        }
    };

    private CharacterCreationListener creationListener = new CharacterCreationListener() {
        @Override
        public void onOptionsFinalized(Player player, int number, EntityRace raceSelected, EntityClass classSelected) {
            firstLogin.add(player.getUniqueId());

            PlayerCharacter newCharacter =
                    PlayerCharacterData.getPlayerCharacters(player.getUniqueId()).addNewCharacter(number, raceSelected, classSelected);

            haveAlreadyLoaded.remove(player.getUniqueId());

            if (newCharacter == null) {
                firstLogin.remove(player.getUniqueId());
                player.kickPlayer(ChatColor.RED + "Something went wrong!");
                return;
            }

            loadCharacter(newCharacter);
        }
    };

    /**
     * Class constructor.
     * @param combatTracker The tracker of whether players are currently in combat or not.
     */
    public PlayerLoader(CombatStatusTracker combatTracker) {
        combatEngine = CombatEngine.getInstance().unsafe().manuallyManagePlayerInitialization();
        this.combatTracker = combatTracker;

        locks = new HashMap<>();
        haveAlreadyLoaded = new HashSet<>();
        firstLogin = new HashSet<>();

        new PlayerListener(this);
    }

    @Override
    public boolean onCharacterSelected(Player player, CharacterId characterId) {
        PlayerCharacters characters = Characters.getInstance().getCharacters(player);
        if (characters == null) {
            LegendsOfValeros.getInstance().getLogger()
                    .severe("A player tried to select a character but no character data was found for them");
            return false;
        }

        PlayerCharacter oldCharacter = (characters.isCharacterLoaded() ? characters.getCurrentCharacter() : null);
        PlayerCharacter newCharacter = characters.getForId(characterId);

        if (newCharacter == null) {
            LegendsOfValeros.getInstance().getLogger()
                    .severe("A player tried to select a character that does not exist for them.");
            MessageUtil.sendError(player, "Invalid selection");
            return false;
        }

        if (newCharacter.equals(oldCharacter)) {
            MessageUtil.sendError(player, "You are already playing that character!");
            return false;
        }

        if (combatTracker.isInCombat(player)) {
            MessageUtil.sendError(player,
                    "You cannot switch characters while in combat! You must stay out of combat for another "
                            + StringUtil.getTimeFromMilliseconds(
                            combatTracker.getMillisUntilOutOfCombat(player), 2, false)
                            + " until you can switch characters.");
            return false;
        }

        if (oldCharacter != null) {
            logoutCharacter(player, false);
        }

        loadCharacter(newCharacter);
        return true;
    }

    @Override
    public boolean onCharacterRemoved(Player player, CharacterId characterId) {
        PlayerCharacters characters = Characters.getInstance().getCharacters(player);
        if (characters != null) {

            if (combatTracker.isInCombat(player)) {
                MessageUtil.sendError(player,
                        "You cannot remove characters while in combat! You must stay out of combat for another "
                                + StringUtil.getTimeFromMilliseconds(
                                combatTracker.getMillisUntilOutOfCombat(player), 2, false)
                                + " until you can remove characters.");
                return false;
            }

            if (Characters.isPlayerCharacterLoaded(player) &&
                    characters.getCurrentCharacter().getCharacterNumber() == characterId.getCharacterNumber()) {
                MessageUtil.sendError(player, "You cannot remove a character that is currently in use!");
                return false;
            }

            PlayerCharacterRemoveEvent event = new PlayerCharacterRemoveEvent(characters.getForId(characterId));
            Bukkit.getPluginManager().callEvent(event);
            characters.removeCharacter(characterId.getCharacterNumber());
        }
        return false;
    }

    @Override
    public boolean onNewCharacterSelected(Player player, int number) {
        PlayerCharacters characters = Characters.getInstance().getCharacters(player);
        if (characters != null) {

            if (combatTracker.isInCombat(player)) {
                MessageUtil
                        .sendError(player, ChatColor.RED
                                + "You cannot switch characters while in combat! You must stay out of combat for another "
                                + StringUtil.getTimeFromMilliseconds(
                                combatTracker.getMillisUntilOutOfCombat(player), 2, false)
                                + " until you can switch characters.");
                return false;
            }

            int max = characters.getMaxCharacters();
            if (characters.size() < max) {
                createNewCharacter(player, number);
                return true;
            } else {
                MessageUtil.sendError(player, ChatColor.RED
                        + "You cannot create any more characters. You have reached your limit of " + max + ".");
                return false;
            }
        }
        return false;
    }

    /**
     * Initiates the loading process, essentially making the player into this character, as soon as
     * they are done loading.
     * @param load The player-character to load.
     */
    public void loadCharacter(PlayerCharacter load) {
        if (load == null) return;

        if (load.isNPC()) {
            onDoneLoading(load.getUniqueCharacterId());
        } else {
            if (load.getPlayer() == null || !load.getPlayer().isOnline()) return;

            TaskPhase<CharacterId> tp =
                    new TaskPhase<>(Characters.getInstance().getUiManager()
                            .getProgressView(load.getPlayer()));

            PlayerCharacterStartLoadingEvent event =
                    new PlayerCharacterStartLoadingEvent(load, tp,
                            haveAlreadyLoaded.add(load.getPlayerId()),
                            firstLogin.contains(load.getPlayerId()));
            Bukkit.getPluginManager().callEvent(event);

            if (tp.hasLocks()) {
                tp.start(load.getUniqueCharacterId(), loadingCallback);

                // if the player is not still under the effect of an initial login lock
                if (!event.isFirstInSession()) {
                    // locks the player during the loading process and clears any previous locks
                    PlayerLock previousLock =
                            locks.put(load.getPlayerId(), PlayerLock.lockPlayer(load.getPlayer()));
                    if (previousLock != null) {
                        previousLock.release();
                    }
                }

            } else {
                onDoneLoading(load.getUniqueCharacterId());
            }
        }
    }

    private void logoutCharacter(Player player, boolean serverLogout) {
        if (Characters.isPlayerCharacterLoaded(player)) {
            PlayerCharacter character = Characters.getPlayerCharacter(player);
            PlayerCharacterLogoutEvent logoutEvent =
                    new PlayerCharacterLogoutEvent(character, serverLogout);

            if (LegendsOfValeros.getInstance().isEnabled()) {
                Bukkit.getPluginManager().callEvent(logoutEvent);

            } else {
                // bypasses Bukkit's stupid system of disabling listeners onDisable. Logs out online
                // player-characters on shutdown. Especially useful and necessary for writing to the db on
                // shutdown.
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    for (RegisteredListener lis : HandlerList.getRegisteredListeners(plugin)) {
                        try {
                            lis.callEvent(logoutEvent);
                        } catch (EventException e) {
                            Logger lg = LegendsOfValeros.getInstance().getLogger();
                            lg.severe("Encountered an issue while manually informing listeners of a player-characters logout on-disable.");
                            MessageUtil.sendException(LegendsOfValeros.getInstance(), player, e, true);
                        }
                    }
                }
            }
        }

        // tells combat engine to stop using the combat data of the character that was just switched
        // away from
        combatEngine.invalidateCombatEntity(player);
    }

    private void onDoneLoading(CharacterId uniqueCharacterId) {
        PlayerCharacter doneLoading = Characters.getInstance().getCharacter(uniqueCharacterId);

        // does not call finish loading event if player is no longer online
        if (doneLoading == null || doneLoading.getPlayer() == null
                || !doneLoading.getPlayer().isOnline()) {
            return;
        }

        PlayerLock playerLock = locks.remove(doneLoading.getPlayerId());
        if (playerLock != null) {
            playerLock.release();
        }

        PlayerCharacterFinishLoadingEvent event =
                new PlayerCharacterFinishLoadingEvent(doneLoading,
                        !haveAlreadyLoaded.contains(doneLoading.getPlayerId()),
                        firstLogin.contains(doneLoading.getPlayerId()));
        Bukkit.getPluginManager().callEvent(event);

        if (firstLogin.contains(doneLoading.getPlayerId()))
            Bukkit.getPluginManager().callEvent(new PlayerCharacterCreateEvent(doneLoading));

        firstLogin.remove(doneLoading.getPlayerId());

        // initializes combat data for the newly loaded player character
        combatEngine.createCombatEntity(doneLoading.getPlayer());
    }

    private void createNewCharacter(Player player, int number) {
        PlayerLock lock = locks.remove(player.getUniqueId());
        if (lock != null) {
            lock.release();
        }

        // Cannot let the user be a character while making a new one.
        logoutCharacter(player, false);

        Characters.getInstance().getUiManager().startCharacterCreation(player, number, creationListener);
    }

    /**
     * Listens to player events that trigger loading behavior.
     */
    private class PlayerListener implements Listener {

        private final PlayerLoader outer;

        private PlayerListener(PlayerLoader outer) {
            this.outer = outer;
            Bukkit.getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            locks.put(event.getPlayer().getUniqueId(), PlayerLock.lockPlayer(event.getPlayer()));

            final ListenableFuture<PlayerCharacters> fut =
                    PlayerCharacterData.onLogin(event.getPlayer().getUniqueId());
            fut.addListener(() -> {
                // syncs to main thread before calling event and launching ui
                Bukkit.getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> {
                    Player player = null;
                    try {
                        PlayerCharacters characters = fut.get();
                        player = characters.getPlayer();
                        if (player == null || !player.isOnline()) {
                            return;
                        }

                        if (characters.size() > 0) {
                            Characters.getInstance().getUiManager().forceCharacterSelection(characters, outer);
                        } else {
                            // automatically makes new character because the player has none
                            createNewCharacter(player, 0);
                        }

                    } catch (Exception e) {
                        System.out.println("[Characters] Could not get base character data.");
                        MessageUtil.sendException(LegendsOfValeros.getInstance(), player, e, true);
                    }
                }, SELECT_DELAY);
            }, Utilities.asyncExecutor());
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerQuit(final PlayerQuitEvent event) {
            haveAlreadyLoaded.remove(event.getPlayer().getUniqueId());
            PlayerLock lock = locks.remove(event.getPlayer().getUniqueId());
            if (lock != null) {
                lock.release();
            }

            logoutCharacter(event.getPlayer(), true);

            // delays invalidating character data so it can be used during logout
            Bukkit.getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> PlayerCharacterData.onLogout(event.getPlayer().getUniqueId()), 1L);
        }

        @EventHandler
        public void onStartLoading(PlayerCharacterStartLoadingEvent event) {
            PlayerCharacter pc = event.getPlayerCharacter();
            InventoryData inventory = pc.getInventoryData();

            PhaseLock lock = event.getLock();

            Bukkit.getScheduler().runTaskAsynchronously(LegendsOfValeros.getInstance(), () -> {
                if (inventory.getData() != null)
                    inventory.loadInventory(pc);
                else
                    inventory.initInventory(pc);

                lock.release();
            });
        }
    }
}
