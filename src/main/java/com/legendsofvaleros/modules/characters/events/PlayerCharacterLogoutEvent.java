package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.loading.TaskPhase;
import org.bukkit.event.HandlerList;

/**
 * An event called when a player stops playing one of their characters.
 * <p>
 * Called either when a player logs out or they change to another character.
 * <p>
 * This event <b>is</b> called on shutdown for any logged-in player characters. Bukkit's default
 * behavior of disabling listeners on-disable is bypassed and listeners that rely on this should
 * still be called, but this should be carefully tested to ensure it works in a specific context.
 */
public class PlayerCharacterLogoutEvent extends PlayerCharacterEvent {

    private static final HandlerList handlers = new HandlerList();

    private final TaskPhase<?> tp;
    private boolean loggedOut;

    public PlayerCharacterLogoutEvent(PlayerCharacter playerCharacter, TaskPhase<?> tp, boolean loggedOutOfServer) {
        super(playerCharacter);
        if (tp == null) {
            throw new IllegalArgumentException("taskphase cannot be null");
        }
        this.tp = tp;
        this.loggedOut = loggedOutOfServer;
    }


    /**
     * Registers an asynchronous task that needs to run before a player's loading screen completes and
     * they are allowed to proceed.
     * <p>
     * Must be called as an immediately and synchronously while this event is being called, or this
     * will do nothing.
     * <p>
     * A player will select log out, and they are then locked in a loading screen. If logging out of the server,
     * it will prevent login until this completes. Any plugins that need to accomplish tasks, such as querying
     * a database for information, register those tasks. Once every registered task is completed, the loading
     * screen ends and the player is allowed to proceed as the character they selected.
     * <p>
     * The player in question could log out at any time before or during the execution of an
     * asynchronous task.
     * <p>
     * As soon as the task's <code>run</code> method returns, its hold on the player ends. For this
     * reason, if a task creates new threads or otherwise needs to be considered active even when
     * <code>run</code> has returned, <code>run</code> either needs not to return until the task is
     * really finished or the lock should be managed manually (see {@link #getLock(String)}).
     * @param task The task to run.
     * @return <code>true</code> if the task was successfully registered. <code>false</code> if the
     * registering failed, such as if the player is not currently in a loading screen.
     */
    public boolean registerLockingTask(String name, Runnable task) {
        return tp.registerTask(name, task);
    }

    /**
     * Gets a lock that will hold the loading screen until it is finished.
     * <p>
     * Must be called as an immediately and synchronously while this event is being called, or this
     * will do nothing.
     * <p>
     * A player will select log out, and they are then locked in a loading screen. If logging out of the server,
     * it will prevent login until this completes. Any plugins that need to accomplish tasks, such as querying
     * a database for information, register those tasks. Once every registered task is completed, the loading
     * screen ends and the player is allowed to proceed as the character they selected.
     * <p>
     * Managing locks manually can lead to bad behavior if done incorrectly. If it is possible to
     * contain your task in a <code>Runnable</code> task, then that is the preferable method. Use
     * locks when it is not possible or reasonable and always make sure to release the lock when your
     * task completes.
     * <p>
     * The player in question could log out at any time before or during the period in which a lock is
     * held.
     * @return A lock for the player. Returns <code>null</code> if the player is not currently in a
     * loading screen.
     */
    public PhaseLock getLock(String name) {
        return tp.getLock(name);
    }

    /**
     * Gets whether the player character is no longer being played because the player logged out of
     * the server altogether or not.
     * @return <code>true</code> if the player logged out of the Minecraft server, <code>false</code>
     * if the player just switched to another character.
     */
    public boolean isServerLogout() {
        return loggedOut;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
