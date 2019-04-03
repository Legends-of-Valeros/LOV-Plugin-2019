package com.legendsofvaleros.modules.combatengine.damage;

import org.bukkit.entity.LivingEntity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.*;

/**
 * Tracks damage on an entity.
 * <p>
 * Does not keep entities in memory.
 * <p>
 * Currently only the last hit on an entity will be considered for kill credit. However, this is
 * likely to change if that simplistic system becomes a problem. As such, encapsulating these
 * features in this generic interface is a prudent step.
 */
public class DamageHistory {

    private WeakReference<LivingEntity> entity;
    private WeakReference<LivingEntity> lastDamager;
    private Map<LivingEntity, Double> damagers = new HashMap<>();

    public LivingEntity getLastDamager() {
        return (lastDamager == null ? null : lastDamager.get());
    }

    public DamageHistory(LivingEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        }
        this.entity = new WeakReference<>(entity);
    }

    /**
     * Gets the killer of this entity, if any other entity should be considered directly responsible.
     * <p>
     * Based on the data currently stored in this object. If instances of damage to the tracked entity
     * were not reported, or {@link #reset()} has been used recently, this data may not be accurate or
     * comprehensive.
     * <p>
     * Resets the data in this object so that damage history is not carried across respawns.
     * @return The entity's killer, if any other entity can be considered responsible for their death,
     * else <code>null</code>. <code>null</code> if the killer is no longer in memory.
     */
    public LivingEntity getKiller() {
        LivingEntity entity = this.entity.get();
        LivingEntity lastDamager;

        if (entity == null || this.lastDamager == null
                || (lastDamager = this.lastDamager.get()) == null) {
            // elements are not in memory or there is not any attributed damage
            reset();
            return null;
        }

        reset();
        return lastDamager;
    }

    /**
     * Resets the history of damage for the tracked entity.
     */
    public void reset() {
        lastDamager = null;
    }

    /**
     * Adds an instance of damage to this history.
     * <p>
     * Should be invoked for all damage on the tracked entity to ensure accuracy.
     * @param damage    The amount of damage done. Should be the final amount of damage done, after
     *                  <i>all</i> modification has been taken into account.
     * @param damagedBy The entity that caused the damage, if any. Can be <code>null</code> if the
     *                  damage was ambiguous or not directly caused by another entity.
     */
    public void reportDamage(double damage, LivingEntity damagedBy) {
        if (damagedBy == null) {
            return;
        }
        if (lastDamager == null || !damagedBy.equals(lastDamager.get())) {
            lastDamager = new WeakReference<>(damagedBy);
        }

        //entity already did damage, add values together
        if (damagers.containsKey(damagedBy)) {
            damagers.put(damagedBy, damagers.get(damagedBy) + damage);
        } else {
            damagers.put(damagedBy, damage);
        }
    }

    /**
     * @return the person that did the most damage in this history
     */
    public LivingEntity getHighestDamager() {
        Entry<LivingEntity, Double> highestDamager = null;
        for (Entry<LivingEntity, Double> ent : damagers.entrySet()) {
            if (ent.getValue() > highestDamager.getValue()) {
                highestDamager = ent;
            }
        }
        return highestDamager.getKey();
    }

}
