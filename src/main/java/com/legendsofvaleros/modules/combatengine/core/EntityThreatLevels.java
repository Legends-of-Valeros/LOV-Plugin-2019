package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityThreat;
import com.legendsofvaleros.modules.combatengine.config.ThreatConfig;
import com.legendsofvaleros.modules.combatengine.events.AIPriorityTargetChangeEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityInvalidatedEvent;
import com.legendsofvaleros.modules.combatengine.events.DamageAddsThreatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Tracks threat levels of this entity's enemies.
 */
public class EntityThreatLevels implements EntityThreat {

    private static final double MIN_THREAT = 0.0;
    private static final double THREAT_PER_DAMAGE = 1.0;
    private static final double MIN_NATURAL_THREAT_IN_RANGE = 0.1;

    private static ThreatConfig config;
    private static CheckTask checkTask;
    private static Set<EntityThreatLevels> trackers;

    static void onEnable(ThreatConfig threatConfig) {
        config = threatConfig;

        trackers = new HashSet<>();

        checkTask = new CheckTask();
        checkTask.runTaskTimer(LegendsOfValeros.getInstance(), config.getValidationCheckTicks(),
                config.getValidationCheckTicks());
    }

    /*static void onDisable() {
        trackers.clear();
        checkTask.cancel();
    }*/

    private final CombatEntity parent;
    private EntityListener listener;

    private PossibleTarget currentTarget;
    private double biggestThreat;

    private final Map<UUID, PossibleTarget> targets;

    EntityThreatLevels(CombatEntity parent) {
        this.parent = parent;
        targets = new HashMap<>();

        // Don't change this to the CombatEngine module's registerEvents. It'll spam the hell out of console.
        Bukkit.getServer().getPluginManager().registerEvents(listener = new EntityListener(), LegendsOfValeros.getInstance());
        //CombatEngine.getInstance().registerEvents(listener = new EntityListener());

        trackers.add(this);
    }

    @Override
    public CombatEntity getTarget() {
        if (currentTarget == null) {
            return null;
        }
        return currentTarget.entity;
    }

    @Override
    public double getThreat(LivingEntity threatFrom) {
        if (threatFrom == null) {
            return 0;
        }

        PossibleTarget target = targets.get(threatFrom.getUniqueId());
        if (target == null) {
            return MIN_THREAT;
        }

        return target.threat;
    }

    @Override
    public void editThreat(LivingEntity threatFrom, double amount) {
        if (threatFrom == null) {
            return;
        }

        PossibleTarget target;
        if (amount <= MIN_THREAT) {
            target = targets.get(threatFrom.getUniqueId());
            if (target == null) {
                return;
            }

        } else {
            target = getOrCreatePossibleTarget(threatFrom);
            if (target == null) {
                return;
            }
        }

        target.threat += amount;

        if (amount > 0) {
            // marks the total threat at increases in order to tick down by percentages of this number
            target.atLastIncrease = target.threat;
        }

        if (target.threat <= MIN_THREAT) {
            remove(threatFrom.getUniqueId());

        } else {
            refreshCurrentTarget(target);
        }
    }

    @Override
    public void setThreat(LivingEntity threatFrom, double amount) {
        if (threatFrom == null) {
            return;
        }

        if (amount <= MIN_THREAT) {
            remove(threatFrom.getUniqueId());

        } else {
            PossibleTarget target = getOrCreatePossibleTarget(threatFrom);
            if (target == null) {
                return;
            }

            if (amount > target.threat) {
                target.atLastIncrease = amount;
            }

            target.threat = amount;

            refreshCurrentTarget(target);
        }
    }

    void onInvalidated() {
        HandlerList.unregisterAll(listener);
        targets.clear();
        trackers.remove(this);
    }

    /**
     * Checks the current target collection and removes any invalid targets.
     */
    private void check() {
        if (targets.isEmpty()) {
            return;
        }

        Iterator<PossibleTarget> targetIter = targets.values().iterator();
        boolean maxRemoved = false;
        while (targetIter.hasNext()) {
            PossibleTarget target = targetIter.next();

            // reduces the target threat by a percentage of what it was the last time threat was increased
            target.threat -= config.getThreatReductionPerCheck() * target.atLastIncrease;

            // if threat is being reduced to 0, but the enemy is still in range, threat is reduced to a
            // very low level instead of lost altogether.
            if (target.threat < MIN_NATURAL_THREAT_IN_RANGE
                    && target.entity.getLivingEntity() != null
                    && parent.getLivingEntity().getLocation()
                    .distanceSquared(target.entity.getLivingEntity().getLocation()) <= config
                    .getMaxTargetingDistanceSquared()) {
                target.threat = MIN_NATURAL_THREAT_IN_RANGE;
            }

            if (!isTargetValid(target)) {

                // the current target was invalidated, need to refresh after checking
                if (currentTarget != null && currentTarget.entity.equals(target.entity)) {
                    maxRemoved = true;
                }
                targetIter.remove();
            }
        }

        if (maxRemoved) {
            refreshCurrentTarget(null);
        }
    }

    /**
     * Makes this check whether a given target is still valid, if they were a target to begin with.
     * @param targetId The unique name of the possibly invalidated target.
     * @param definite <code>true</code> if there is no need to check validity because the target has
     *                 definitely become invalid.
     */
    private void onPossibleInvalidation(UUID targetId, boolean definite) {
        PossibleTarget target = targets.get(targetId);
        if (target != null) {
            if (definite || !isTargetValid(target)) {
                remove(targetId);
            }
        }
    }

    private void refreshCurrentTarget(PossibleTarget candidate) {
        if (candidate == null) {
            PossibleTarget biggest = null;
            double max = 0;
            for (PossibleTarget target : targets.values()) {
                if (target.threat > max) {
                    max = target.threat;
                    biggest = target;
                }
            }
            biggestThreat = max;
            setTarget(biggest);

        } else {
            if (candidate.threat > biggestThreat) {
                biggestThreat = candidate.threat;
                setTarget(candidate);
            }
        }
    }

    private void remove(UUID targetId) {
        if (targetId == null) {
            return;
        }

        targets.remove(targetId);

        // if the removed target was the current target
        if (currentTarget != null && currentTarget.entity.getUniqueId().equals(targetId)) {
            refreshCurrentTarget(null);
        }
    }

    // returns null if not possible to create
    private PossibleTarget getOrCreatePossibleTarget(LivingEntity entity) {
        PossibleTarget target = targets.get(entity.getUniqueId());
        if (target == null) {
            CombatEntity ce = CombatEngine.getEntity(entity);
            if (ce == null)
                return null;

            target = new PossibleTarget(ce);
            target.threat = MIN_THREAT + 0.00001;

            // does not create impossible targets
            if (!isTargetValid(target)) {
                return null;
            }

            targets.put(entity.getUniqueId(), target);
        }
        return target;
    }

    private boolean isTargetValid(PossibleTarget target) {
        LivingEntity parentLe;
        LivingEntity targetLe;

        // invalid if either entity is not currently active/in memory
        if (!parent.isActive() || (parentLe = parent.getLivingEntity()) == null || target == null
                || !target.entity.isActive() || (targetLe = target.entity.getLivingEntity()) == null) {
            return false;
        }

        // invalid if either entity is dead or if the entities are too far apart
        if (parentLe.isDead()
                || targetLe.isDead()
                || parent.getLivingEntity().getLocation().distanceSquared(targetLe.getLocation()) > config
                .getMaxTargetingDistanceSquared()) {
            return false;
        }

        if (target.entity.isPlayer()) {
            switch (((Player) target.entity.getLivingEntity()).getGameMode()) {
                case SPECTATOR:
                case CREATIVE:
                    return false;
                default:
                    break;
            }
        }

        // invalid if not enough threat to keep relationship in memory
        return target.threat > MIN_THREAT;

    }

    private void setTarget(PossibleTarget newTarget) {

        // if no actual change, returns
        if (currentTarget == newTarget
                || (currentTarget != null && newTarget != null && currentTarget.entity
                .equals(newTarget.entity))) {
            return;
        }

        CombatEntity newCe = newTarget != null ? newTarget.entity : null;
        CombatEntity currentCe = currentTarget != null ? currentTarget.entity : null;

        AIPriorityTargetChangeEvent event = new AIPriorityTargetChangeEvent(parent, newCe, currentCe);
        Bukkit.getPluginManager().callEvent(event);

        currentTarget = newTarget;
    }

    /**
     * An enemy entity who is threatening to this entity and might be targeted.
     */
    private class PossibleTarget {
        private final CombatEntity entity;
        private double threat;
        private double atLastIncrease;

        private PossibleTarget(CombatEntity entity) {
            if (entity == null) {
                throw new NullPointerException();
            }
            this.entity = entity;
        }
    }

    /**
     * Listens for events that could invalidate this entity's possible targets.
     * <p>
     * Also automatically adds threat for damage done to the entity this tracker is for.
     */
    private class EntityListener implements Listener {

        // adds threat for any damage dealt
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onCombatEngineDamage(CombatEngineDamageEvent event) {

            // if the damage was by another entity and on this tracker's entity
            if (event.getAttacker() != null && event.getDamaged().equals(parent)) {

                double threatToAdd = THREAT_PER_DAMAGE * event.getFinalDamage();

                // calls threat added event in order to allow for the default threat-to-damage ratio to be
                // edited/cancelled
                DamageAddsThreatEvent threatEvent =
                        new DamageAddsThreatEvent(event.getDamaged(), event.getAttacker(), getThreat(event
                                .getAttacker().getLivingEntity()), threatToAdd);
                Bukkit.getPluginManager().callEvent(threatEvent);

                if (!threatEvent.isCancelled()) {
                    editThreat(event.getAttacker().getLivingEntity(), threatEvent.getThreatAdded());
                }
            }
        }

        // adds a minimal amount of threat on natural, vanilla targeting (if it happens)
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
            if (event.getEntity().getUniqueId().equals(parent.getUniqueId()) && event.getTarget() != null) {

                PossibleTarget target = getOrCreatePossibleTarget(event.getTarget());
                if (target != null && target.threat <= MIN_NATURAL_THREAT_IN_RANGE) {
                    target.threat = MIN_NATURAL_THREAT_IN_RANGE;
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPlayerTeleport(PlayerTeleportEvent event) {
            onPossibleInvalidation(event.getPlayer().getUniqueId(), false);
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            onPossibleInvalidation(event.getPlayer().getUniqueId(), true);
        }

        @EventHandler
        public void onCombatEntityInvalidated(CombatEntityInvalidatedEvent event) {
            onPossibleInvalidation(event.getInvalidatedUuid(), true);
        }

        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            onPossibleInvalidation(event.getEntity().getUniqueId(), true);
        }
    }

    /**
     * Periodically makes sure that targets are valid.
     */
    private static class CheckTask extends BukkitRunnable {
        @Override
        public void run() {
            for (EntityThreatLevels tracker : trackers) {
                tracker.check();
            }
        }
    }

}
