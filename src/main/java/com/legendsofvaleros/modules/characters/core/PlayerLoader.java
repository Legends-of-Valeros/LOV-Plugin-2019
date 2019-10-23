package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.util.StringUtil;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.events.*;
import com.legendsofvaleros.modules.characters.loading.Callback;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.loading.PlayerLock;
import com.legendsofvaleros.modules.characters.loading.TaskPhase;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.characters.ui.CharacterCreationListener;
import com.legendsofvaleros.modules.characters.ui.CharacterSelectionListener;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.UnsafePlayerInitializer;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
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
    private Set<UUID> firstLogin;

    private Callback<CharacterId> loadingCallback = (value, error) -> {
        if (error != null) {
            Characters.getInstance().getLogger().severe("Error while loading a player's character data");
            MessageUtil.sendSevereException(Characters.getInstance(), error);
        } else if (value != null) {
            Characters.getInstance().getScheduler().executeInSpigotCircle(() -> onDoneLoading(value));
        }
    };

    private Callback<CharacterId> logoutCallback = (value, error) -> {
        if (error != null) {
            Characters.getInstance().getLogger().severe("Error while logging out a player's character data");
            MessageUtil.sendSevereException(Characters.getInstance(), error);
        } else if (value != null) {
            Characters.getInstance().getScheduler().executeInSpigotCircle(() -> onDoneLoggingOut(value));
        }
    };

    private CharacterCreationListener creationListener = new CharacterCreationListener() {
        @Override
        public void onOptionsFinalized(Player player, int number, EntityRace raceSelected, EntityClass classSelected) {
            firstLogin.add(player.getUniqueId());

            PlayerCharacter newCharacter =
                    PlayerCharacterData.getPlayerCharacters(player.getUniqueId()).addNewCharacter(number, raceSelected, classSelected);

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
        firstLogin = new HashSet<>();

        new PlayerListener(this);
    }

    @Override
    public boolean onCharacterSelected(Player player, CharacterId characterId) {
        PlayerCharacters characters = Characters.getInstance().getCharacters(player);
        if (characters == null) {
            Characters.getInstance().getLogger().severe("A player tried to select a character but no character data was found for them");
            return false;
        }

        PlayerCharacter oldCharacter = (characters.isCharacterLoaded() ? characters.getCurrentCharacter() : null);
        PlayerCharacter newCharacter = characters.getForId(characterId);

        if (newCharacter == null) {
            Characters.getInstance().getLogger()
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
            logoutCharacter(player, false).on((err, val) -> {
                loadCharacter(newCharacter);
            }, Characters.getInstance().getScheduler()::sync);
        } else {
            loadCharacter(newCharacter);
        }
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

            Characters.getInstance().getLogger().info(load.getPlayer().getDisplayName() + " is loading his character: " + load.getUniqueCharacterId());

            TaskPhase<CharacterId> tp =
                    new TaskPhase<>("Login", Characters.getInstance().getUiManager()
                            .getProgressView(load.getPlayer()));

            PlayerCharacterStartLoadingEvent event =
                    new PlayerCharacterStartLoadingEvent(load, tp,
                            firstLogin.contains(load.getPlayerId()));
            Bukkit.getPluginManager().callEvent(event);

            if (tp.hasLocks()) {
                tp.start(load.getUniqueCharacterId(), loadingCallback);

                // locks the player during the loading process and clears any previous locks
                PlayerLock previousLock = locks.put(load.getPlayerId(), PlayerLock.lockPlayer(load.getPlayer()));
                if (previousLock != null) {
                    previousLock.release();
                }
            } else {
                onDoneLoading(load.getUniqueCharacterId());
            }
        }
    }

    private void onDoneLoading(CharacterId uniqueCharacterId) {
        PlayerCharacter doneLoading = Characters.getInstance().getCharacter(uniqueCharacterId);

        PlayerLock playerLock = locks.remove(doneLoading.getPlayerId());
        if (playerLock != null) {
            playerLock.release();
        }

        // does not call finish loading event if player is no longer online
        if (doneLoading == null || doneLoading.getPlayer() == null
                || !doneLoading.getPlayer().isOnline()) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new PlayerCharacterFinishLoadingEvent(doneLoading,
                firstLogin.contains(doneLoading.getPlayerId())));

        if (firstLogin.contains(doneLoading.getPlayerId())) {
            Bukkit.getPluginManager().callEvent(new PlayerCharacterCreateEvent(doneLoading));
        }

        firstLogin.remove(doneLoading.getPlayerId());

        // initializes combat data for the newly loaded player character
        combatEngine.createCombatEntity(doneLoading.getPlayer());
    }

    private Promise<Boolean> logoutCharacter(Player player, boolean serverLogout) {
        Promise<Boolean> promise = new Promise<>();

        if (!Characters.isPlayerCharacterLoaded(player)) {
            promise.resolve(false);
        } else {
            Characters.getInstance().getLogger().info(player.getDisplayName() + " is logging out of his character...");

            PlayerCharacter character = Characters.getPlayerCharacter(player);

            TaskPhase<CharacterId> tp =
                    new TaskPhase<>("Logout");

            PlayerCharacterLogoutEvent event =
                    new PlayerCharacterLogoutEvent(character, tp, serverLogout);

            if (LegendsOfValeros.getInstance().isEnabled()) {
                Bukkit.getPluginManager().callEvent(event);

            } else {
                // bypasses Bukkit's stupid system of disabling listeners onDisable. Logs out online
                // player-characters on shutdown. Especially useful and necessary for writing to the db on
                // shutdown.
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    for (RegisteredListener lis : HandlerList.getRegisteredListeners(plugin)) {
                        try {
                            lis.callEvent(event);
                        } catch (EventException e) {
                            Logger lg = Characters.getInstance().getLogger();
                            lg.severe("Encountered an issue while manually informing listeners of a player-characters logout on-disable.");
                            MessageUtil.sendSevereException(Characters.getInstance(), player, e);
                        }
                    }
                }
            }

            if (tp.hasLocks()) {
                tp.start(character.getUniqueCharacterId(), (value, error) -> {
                    logoutCallback.callback(value, error);

                    promise.resolve(true);
                });

                // locks the player during the logout process and clears any previous locks.
                // This also prevents logging back in until your logout process completes.
                PlayerLock previousLock =
                        locks.put(character.getPlayerId(), PlayerLock.lockPlayer(character.getPlayer()));
                if (previousLock != null) {
                    previousLock.release();
                }

            } else {
                onDoneLoggingOut(character.getUniqueCharacterId());
                promise.resolve(true);
            }
        }

        // tells combat engine to stop using the combat data of the character that was just switched
        // away from
        combatEngine.invalidateCombatEntity(player);

        return promise;
    }

    private void onDoneLoggingOut(CharacterId uniqueCharacterId) {
        PlayerLock playerLock = locks.remove(uniqueCharacterId.getPlayerId());
        if (playerLock != null) {
            playerLock.release();
        }
    }

    private void createNewCharacter(Player player, int number) {
        PlayerLock lock = locks.remove(player.getUniqueId());
        if (lock != null) {
            lock.release();
        }

        // Cannot let the user be a character while making a new one.
        logoutCharacter(player, false).on((err, val) -> {
            Characters.getInstance().getUiManager().startCharacterCreation(player, number, creationListener);
        }, Characters.getInstance().getScheduler()::sync);
    }

    /**
     * Listens to player events that trigger loading behavior.
     */
    private class PlayerListener implements Listener {

        private final PlayerLoader outer;

        private PlayerListener(PlayerLoader outer) {
            this.outer = outer;
            Characters.getInstance().registerEvents(this);
        }

        @EventHandler
        public void onPlayerLogin(PlayerLoginEvent event) {
            // If a lock is still active, then processing is still being done for an old
            // player character. We don't want them to join, right now.
            if (locks.containsKey(event.getPlayer().getUniqueId())) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Logging in too quickly! Try again in a moment.");
            }
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            locks.put(event.getPlayer().getUniqueId(), PlayerLock.lockPlayer(event.getPlayer()));

            PlayerCharacterData.onLogin(event.getPlayer().getUniqueId()).onSuccess(val -> {
                if (!val.isPresent()) {
                    return;
                }

                PlayerCharacters characters = val.get();

                Player player = characters.getPlayer();
                if (player == null || !player.isOnline()) {
                    return;
                }

                if (characters.size() > 0) {
                    Characters.getInstance().getUiManager().forceCharacterSelection(characters, outer);
                } else {
                    // automatically makes new character because the player has none
                    createNewCharacter(player, 0);
                }
            }, Characters.getInstance().getScheduler()::sync);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerLogin(PlayerCharacterStartLoadingEvent event) {
            PlayerCharacter pc = event.getPlayerCharacter();
            InventoryData inventory = pc.getInventoryData();

            if(inventory.getData() != null)
                inventory.loadInventory(pc);
            else
                inventory.initInventory(pc);
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerLogout(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Character");

            PlayerCharacterData.onLogout(event.getPlayerCharacter()).on(lock::release);
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onServerLogout(PlayerQuitEvent event) {
            logoutCharacter(event.getPlayer(), true).on(() ->
                    PlayerCharacterData.onLogout(event.getPlayer().getUniqueId()));
        }
    }
}
