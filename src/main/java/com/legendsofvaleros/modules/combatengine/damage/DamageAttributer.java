package com.legendsofvaleros.modules.combatengine.damage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.legendsofvaleros.modules.combatengine.config.DamageAttributionConfig;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tracks damage done to attribute kills.
 */
public class DamageAttributer {

  private Cache<UUID, DamageHistory> damageCache;
  public DamageHistory getDamageHistory(LivingEntity entity) { return damageCache.getIfPresent(entity.getUniqueId()); }

  // squared distances are more efficient
  private double maxDistanceSquared;

  /**
   * Class constructor.
   */
  public DamageAttributer(DamageAttributionConfig config) {
    damageCache =
        CacheBuilder.newBuilder()
            .expireAfterAccess(config.getHistoryExpirationMillis(), TimeUnit.MILLISECONDS)
            .concurrencyLevel(1).build();

    maxDistanceSquared = config.getMaxKillDistance() * config.getMaxKillDistance();
    CombatEngine.getInstance().registerEvents(new CleanupListener());
  }

  /**
   * Reports damage from one entity on another.
   * <p>
   * Should be called for all instances of damage in order to assure accuracy.
   * 
   * @param damaged The entity that was damaged.
   * @param damager The entity that caused the damage. Can be <code>null</code> if the cause of the
   *        damage is ambiguous or not caused by another entity directly.
   * @param amount The amount of damage done.
   */
  public void reportDamage(LivingEntity damaged, LivingEntity damager, double amount) {
    DamageHistory history = getDamageHistory(damaged);
    if (history == null) {
      history = new DamageHistory(damaged);
      damageCache.put(damaged.getUniqueId(), history);
    }
    history.reportDamage(amount, damager);
  }

  /**
   * Called when an entity dies and gets their killer, if there is one.
   * 
   * @param died The entity that died.
   * @return The entity's killer, if there was a killer. <code>null</code> if no entity met the
   *         requirements to get credit for the kill.
   */
  LivingEntity onDeath(LivingEntity died) {
    DamageHistory history = damageCache.getIfPresent(died.getUniqueId());
    if (history == null) {
      return null;
    } else if (died.getType() != EntityType.PLAYER) {
      damageCache.invalidate(died.getUniqueId());
    }
    LivingEntity killer = history.getKiller();

    if (killer != null && !died.equals(killer)
        && killer.getLocation().distanceSquared(died.getLocation()) <= maxDistanceSquared) {
      return killer;
    } else {
      return null;
    }
  }

  /**
   * Listens for events that invalidate cached damage history.
   */
  private class CleanupListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
      damageCache.invalidate(event.getPlayer().getUniqueId());
    }

  }

}
