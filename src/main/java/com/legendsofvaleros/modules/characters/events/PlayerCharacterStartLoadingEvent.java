package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.loading.TaskPhase;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.loading.TaskPhase;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.loading.TaskPhase;
import org.bukkit.event.HandlerList;

/**
 * An event called when a player goes into a loading screen while their character's data is loading.
 * <p>
 * Clients can use this event to to register tasks and/or get locks for a player character's loading
 * phase and make sure the player does not start playing the character until all essential data is
 * loaded.
 * <p>
 * This is never called if the player is an NPC.
 */
public class PlayerCharacterStartLoadingEvent extends PlayerCharacterEvent {

	private static final HandlerList handlers = new HandlerList();

	private final TaskPhase<?> tp;
	private final boolean firstInSession;
	private final boolean firstLogin;

	public PlayerCharacterStartLoadingEvent(PlayerCharacter loadingFor,
			TaskPhase<?> tp, boolean firstInSession, boolean firstLogin) throws IllegalArgumentException {
		super(loadingFor);
		if (tp == null) {
			throw new IllegalArgumentException("taskphase cannot be null");
		}
		this.tp = tp;
		this.firstInSession = firstInSession;
		this.firstLogin = firstLogin;
	}

	/**
	 * Registers an asynchronous task that needs to run before a player's loading screen completes and
	 * they are allowed to proceed.
	 * <p>
	 * Must be called as an immediately and synchronously while this event is being called, or this
	 * will do nothing.
	 * <p>
	 * A player will select a character, and they are then locked in a loading screen. Any plugins
	 * that need to accomplish tasks, such as querying a database for information, register those
	 * tasks. Once every registered task is completed, the loading screen ends and the player is
	 * allowed to proceed as the character they selected.
	 * <p>
	 * The player in question could log out at any time before or during the execution of an
	 * asynchronous task.
	 * <p>
	 * As soon as the task's <code>run</code> method returns, its hold on the player ends. For this
	 * reason, if a task creates new threads or otherwise needs to be considered active even when
	 * <code>run</code> has returned, <code>run</code> either needs not to return until the task is
	 * really finished or the lock should be managed manually (see {@link #getLock()}).
	 * 
	 * @param task The task to run.
	 * @return <code>true</code> if the task was successfully registered. <code>false</code> if the
	 *         registering failed, such as if the player is not currently in a loading screen.
	 */
	public boolean registerLockingTask(Runnable task) {
		return tp.registerTask(task);
	}

	/**
	 * Gets a lock that will hold the loading screen until it is finished.
	 * <p>
	 * Must be called as an immediately and synchronously while this event is being called, or this
	 * will do nothing.
	 * <p>
	 * A player will select a character, and they are then locked in a loading screen. Any plugins
	 * that need to accomplish tasks, such as querying a database for information, register those
	 * tasks. Once every registered task is completed, the loading screen ends and the player is
	 * allowed to proceed as the character they selected.
	 * <p>
	 * Managing locks manually can lead to bad behavior if done incorrectly. If it is possible to
	 * contain your task in a <code>Runnable</code> task, then that is the preferable method. Use
	 * locks when it is not possible or reasonable and always make sure to release the lock when your
	 * task completes.
	 * <p>
	 * The player in question could log out at any time before or during the period in which a lock is
	 * held.
	 *
	 * @return A lock for the player. Returns <code>null</code> if the player is not currently in a
	 *         loading screen.
	 */
	public PhaseLock getLock() {
		return tp.getLock();
	}

	/**
	 * Gets whether this is the first time a player has successfully started playing a character since
	 * they logged into this Minecraft server.
	 * 
	 * @return <code>true</code> if this is the first time a player finished loading their character
	 *         since logging into this MC server. <code>false</code> if they switched from another
	 *         character.
	 */
	public boolean isFirstInSession() {
		return firstInSession;
	}
	
	public boolean isFirstLogin() {
		return firstLogin;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
