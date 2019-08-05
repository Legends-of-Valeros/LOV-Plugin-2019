package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * An event called when a player-character logs out while in combat.
 * <p>
 * Cancellable. Cannot stop a player from logging out, but can stop other listeners from being
 * informed of this event, such as if there is a special circumstance in which something that looks
 * like combat logging should not be treated as combat logging.
 * <p>
 * Called as soon as possible after a player starts the logout process in order to preempt
 * data-saving and other logout tasks.
 */
public class PlayerCharacterCombatLogoutEvent extends PlayerCharacterEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final boolean pvp;
    private final boolean pve;

    private boolean cancelled;

    /**
     * Class constructor.
     * @param combatLogged The player-character that logged out during combat.
     * @param fromPvp      <code>true</code> if the player-character logged out during PvP combat. Else
     *                     <code>false</code>.
     * @param fromPve      <code>true</code> if the player-character logged out during PvE combat. Else
     *                     <code>false</code>.
     */
    public PlayerCharacterCombatLogoutEvent(PlayerCharacter combatLogged, boolean fromPvp,
                                            boolean fromPve) {
        super(combatLogged);
        this.pvp = fromPvp;
        this.pve = fromPve;
    }

    /**
     * Gets whether the player-character that logged out was in PvP combat at the time they combat
     * logged.
     * <p>
     * It is possible for a player-character to be considered in <i>both</i> PvP and PvE combat at the
     * time they logged out.
     * @return <code>true</code> if the player-character that logged out was recently fighting a
     * player.
     */
    public boolean wasInPvp() {
        return pvp;
    }

    /**
     * Gets whether the player-character that logged out was in PvE combat at the time they combat
     * logged.
     * <p>
     * It is possible for a player-character to be considered in <i>both</i> PvP and PvE combat at the
     * time they logged out.
     * @return <code>true</code> if the player-character that logged out was recently in combat with a
     * mob or the environment.
     */
    public boolean wasInPve() {
        return pve;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
