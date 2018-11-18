package com.legendsofvaleros.modules.combatengine.statuseffects;

import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder.ModifierType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stops entities from using skills, spells, moving, interacting, and doing just about anything.
 * <p>
 * Negates their hit chance and speed stats to help the stun effect.
 */
public class Stun {

  // TODO particle effects?

  private static final long TELEPORT_PERIOD = 10;

  private static final double SPEED_MULTIPLIER = 0;
  private static final double HIT_MULTIPLIER = 0;

  private static final Map<UUID, Location> STARTING_LOCS = new HashMap<>();
  private static final Multimap<UUID, ValueModifier> MODS = HashMultimap.create();
  private static final Cache<UUID, LivingEntity> ENTITIES = CacheBuilder.newBuilder()
      .concurrencyLevel(1).weakValues().build();

  static {
    new MovementStopper();
    new NoInteractionListener(STARTING_LOCS.keySet());
  }

  public static void apply(CombatEntity entity) {
    remove(entity);

    LivingEntity le = entity.getLivingEntity();
    UUID id = entity.getUniqueId();
    if (le != null && entity.isActive()) {
      STARTING_LOCS.put(id, le.getLocation());
      ENTITIES.put(id, le);

      MODS.put(
          id,
          entity.getStats().newStatModifierBuilder(Stat.SPEED)
              .setModifierType(ModifierType.MULTIPLIER).setValue(SPEED_MULTIPLIER).build());

      MODS.put(
          id,
          entity.getStats().newStatModifierBuilder(Stat.HIT_CHANCE)
              .setModifierType(ModifierType.MULTIPLIER).setValue(HIT_MULTIPLIER).build());
    }
  }

  public static void remove(CombatEntity entity) {

    STARTING_LOCS.remove(entity.getUniqueId());
    ENTITIES.invalidate(entity.getUniqueId());

    Collection<ValueModifier> mods = MODS.removeAll(entity.getUniqueId());
    if (mods != null) {
      for (ValueModifier mod : mods) {
        mod.remove();
      }
    }
  }

  /**
   * Periodically teleports entities back to where they were initially stunned.
   * <p>
   * Listening to and modifying player movement events is very computationally expensive and can be
   * complex. Periodic teleportating is often seen as the most reasonable solution for stopping
   * movement.
   */
  private static class MovementStopper extends BukkitRunnable {

    private MovementStopper() {
      runTaskTimer(LegendsOfValeros.getInstance(), TELEPORT_PERIOD, TELEPORT_PERIOD);
    }

    @Override
    public void run() {
      if (!STARTING_LOCS.isEmpty()) {
        for (Map.Entry<UUID, Location> ent : STARTING_LOCS.entrySet()) {
          LivingEntity le = ENTITIES.getIfPresent(ent.getKey());
          if (le != null) {
            Location loc = ent.getValue();

            // allows the entity to fall while stunned.
            double newY = le.getLocation().getY();
            if (loc.getY() > newY) {
              loc.setY(newY);
            }

            le.teleport(loc);
          }
        }
      }
    }

  }

}
