package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.config.CombatLoggingConfig;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterCombatLogoutEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks when players are in combat.
 * <p>
 * Implements infrastructure for punishing players that log out while in combat.
 * <p>
 * Calls combat-logout events as early as possible in the logout process in order for penalties to
 * be applied before data is saved.
 */
public class CombatStatusTracker {

  private CombatLoggingConfig config;

  // both incoming and outgoing damage are counted as combat
  private Map<UUID, Long> lastPvpDamage;
  private Map<UUID, Long> lastPveDamage;

  public CombatStatusTracker(CombatLoggingConfig config) {
    this.config = config;
    this.lastPvpDamage = new HashMap<>();
    this.lastPveDamage = new HashMap<>();

    Bukkit.getPluginManager().registerEvents(new CombatStatusListener(), LegendsOfValeros.getInstance());
  }

  /**
   * Gets whether a player has dealt or received damage (either PvP or PvE) recently enough for them
   * to be considered currently in combat.
   * 
   * @param player The player to get whether they should be considered currently in combat.
   * @return <code>true</code> if the player is currently in combat, else <code>false</code>.
   */
  public boolean isInCombat(Player player) {
    return isInPveCombat(player) || isInPvpCombat(player);
  }

  /**
   * Gets how long, in milliseconds, a player must wait until they are considered out of combat.
   * <p>
   * The timer will be reset if they engage in combat actions between now and the expiration.
   * 
   * @param player The player to get how long without engaging in combat until they are considered
   *        out of combat.
   * @return The number of milliseconds out of combat the player needs to wait until they are no
   *         longer considered in combat. <code>0</code> if the player is not currently in combat.
   */
  public long getMillisUntilOutOfCombat(Player player) {
    long pve = getMillisUntilOutOfPveCombat(player);
    long pvp = getMillisUntilOutOfPvpCombat(player);
    return Math.max(pve, pvp);
  }

  /**
   * Gets whether a player has dealt or received PvP damage recently enough for them to be
   * considered currently in PvP combat (combat with other players).
   * <p>
   * This method ignores combat with the environment or with npc mobs.
   * 
   * @param player The player to get whether they should be considered currently in PvP combat.
   * @return <code>true</code> if the player is currently in PvP combat, else <code>false</code>.
   */
  public boolean isInPvpCombat(Player player) {
    if (player == null) {
      return false;
    }
    Long lastDamage = lastPvpDamage.get(player.getUniqueId());
    if (lastDamage == null) {
      return false;
    }

    return System.currentTimeMillis() - lastDamage < config
        .getMillisUntilOutOfPvpCombat();
  }

  /**
   * Gets how long, in milliseconds, a player must wait until they are considered out of PvP combat
   * (combat with other players).
   * <p>
   * The timer will be reset if they engage in PvP combat actions between now and the expiration.
   * <p>
   * This method ignores combat with the environment or with npc mobs.
   * 
   * @param player The player to get how long without engaging in PvP combat until they are
   *        considered out of PvP combat.
   * @return The number of milliseconds out of PvP combat the player needs to wait until they are no
   *         longer considered in PvP combat. <code>0</code> if the player is not currently in
   *         combat.
   */
  public long getMillisUntilOutOfPvpCombat(Player player) {
    if (player == null) {
      return 0;
    }
    Long lastDamage = lastPvpDamage.get(player.getUniqueId());
    if (lastDamage == null) {
      return 0;
    }
    long timePassed = System.currentTimeMillis() - lastDamage;
    long remaining = config.getMillisUntilOutOfPvpCombat() - timePassed;
    return Math.max(0, remaining);
  }

  /**
   * Gets whether a player has dealt or received PvE damage recently enough for them to be
   * considered currently in PvE combat (combat with mobs and/or the environment).
   * <p>
   * This method ignores combat with players.
   * 
   * @param player The player to get whether they should be considered currently in PvE combat.
   * @return <code>true</code> if the player is currently in PvE combat, else <code>false</code>.
   */
  public boolean isInPveCombat(Player player) {
    if (player == null) {
      return false;
    }
    Long lastDamage = lastPveDamage.get(player.getUniqueId());
    if (lastDamage == null) {
      return false;
    }

    return System.currentTimeMillis() - lastDamage < config
        .getMillisUntilOutOfPveCombat();
  }

  /**
   * Gets how long, in milliseconds, a player must wait until they are considered out of PvE combat
   * (combat with mobs and/or the environment).
   * <p>
   * The timer will be reset if they engage in PvE combat actions between now and the expiration.
   * <p>
   * This method ignores combat with players.
   * 
   * @param player The player to get how long without engaging in PvE combat until they are
   *        considered out of PvE combat.
   * @return The number of milliseconds out of PvE combat the player needs to wait until they are no
   *         longer considered in PvE combat. <code>0</code> if the player is not currently in
   *         combat.
   */
  public long getMillisUntilOutOfPveCombat(Player player) {
    if (player == null) {
      return 0;
    }
    Long lastDamage = lastPveDamage.get(player.getUniqueId());
    if (lastDamage == null) {
      return 0;
    }
    long timePassed = System.currentTimeMillis() - lastDamage;
    long remaining = config.getMillisUntilOutOfPveCombat() - timePassed;
    return Math.max(0, remaining);
  }

  /**
   * Forcibly resets a player's combat status so they are immediately considered to be out of
   * combat.
   * 
   * @param reset The player who should be immediately considered out of combat.
   */
  public void resetCombatStatus(Player reset) {
    if (reset == null) {
      return;
    }
    lastPvpDamage.remove(reset.getUniqueId());
    lastPveDamage.remove(reset.getUniqueId());
  }

  /**
   * Tracks instances of damage and logouts.
   */
  private class CombatStatusListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatEngineDamage(CombatEngineDamageEvent event) {

      boolean defenderPlayer = event.getDamaged().isPlayer();
      boolean attackerPlayer = event.getAttacker() != null && event.getAttacker().isPlayer();

      // no player involved, returns
      if (!defenderPlayer && !attackerPlayer) {
        return;
      }

      boolean pvp = defenderPlayer && attackerPlayer;
      Map<UUID, Long> trackIn = (pvp ? lastPvpDamage : lastPveDamage);
      long now = System.currentTimeMillis();

      if (defenderPlayer) {
        trackIn.put(event.getDamaged().getUniqueId(), now);
      }
      if (attackerPlayer) {
        trackIn.put(event.getAttacker().getUniqueId(), now);
      }
    }

    // listens on low (second-called) priority to preempt data saving and have penalties for
    // combat logging persist
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {

      boolean pvp = isInPvpCombat(event.getPlayer());
      boolean pve = isInPveCombat(event.getPlayer());

      PlayerCharacter pc = (Characters.isPlayerCharacterLoaded(event.getPlayer()) ? Characters.getPlayerCharacter(event.getPlayer()) : null);

      // only calls event if plugin is not enabled to make sure players are not penalized for being
      // kicked on shutdown
      if ((pvp || pve) && pc != null && LegendsOfValeros.getInstance().isEnabled()) {
        PlayerCharacterCombatLogoutEvent combatLogEvent =
            new PlayerCharacterCombatLogoutEvent(pc, pvp, pve);
        Bukkit.getPluginManager().callEvent(combatLogEvent);
      }

      lastPvpDamage.remove(event.getPlayer().getUniqueId());
      lastPveDamage.remove(event.getPlayer().getUniqueId());
    }

  }

}
