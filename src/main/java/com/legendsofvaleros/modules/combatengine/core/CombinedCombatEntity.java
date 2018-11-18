package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineStatusEffectAddedEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineStatusEffectRemovedEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ConstructionListener;
import com.legendsofvaleros.modules.combatengine.modifiers.DecayingValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStatValue;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.combatengine.stat.StatValue;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.api.EntityStatusEffects;
import com.legendsofvaleros.modules.combatengine.api.EntityThreat;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType.RemovalReason;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.ui.PlayerCombatInterface;
import com.legendsofvaleros.modules.combatengine.ui.RegeneratingStatChangeListener;
import com.legendsofvaleros.modules.combatengine.ui.StatChangeListener;
import com.legendsofvaleros.modules.npcs.NPCs;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Tracks combat data about an entity.
 * <p>
 * Does not unnecessarily maintain references to modifiers that affect this. If this class has no
 * specifically defined reason to remove a modifier, then it does not need to keep track of it and
 * make sure it stays in memory. For non-expiring edits, the onus is on the client to keep the
 * modifier in memory if they wish to reverse it.
 */
public class CombinedCombatEntity implements CombatEntity, EntityStats, EntityStatusEffects,
        ConstructionListener {

    // TODO check if the entity is in memory every time this is interacted with? (or at least when
    // isActive() is called?). Invalidation happens when the cache is accessed, not as soon as the
    // entity is garbage collected.

    private final UUID uid;
    private WeakReference<LivingEntity> entity;
    private final boolean isPlayer;

    private final Map<Stat, StatValue> stats;
    private final Map<RegeneratingStat, RegeneratingStatValue> regeneratingStats;
    private final Map<StatusEffectType, StatusEffectTask> activeEffects;
    private final Set<ValueModifier> clearOnDeath;
    private final Set<ValueModifier> haveTasks;

    private final EntityThreatLevels threat;

    private final ChangeListener listener;
    private final MinecraftHealthHandler healthHandler;
    private final SpeedEngine speedEngine;
    private PlayerCombatInterface ui;

    private boolean dead;
    private boolean active;

    /**
     * Class constructor.
     * @param entity The entity these stats are for.
     * @throws IllegalArgumentException On a <code>null</code> entity.
     */
    public CombinedCombatEntity(LivingEntity entity) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        }
        this.uid = entity.getUniqueId();
        this.entity = new WeakReference<>(entity);
        this.isPlayer = entity.getType() == EntityType.PLAYER && !NPCs.isNPC(entity);

        this.listener = new ChangeListener(this);
        this.healthHandler = CombatEngine.getInstance().getMinecraftHealthHandler();
        this.speedEngine = CombatEngine.getInstance().getSpeedEngine();

        if (!this.isPlayer) {
            this.threat = new EntityThreatLevels(this);
        } else {
            this.threat = null;
        }

        this.stats = new HashMap<>();
        this.regeneratingStats = new HashMap<>();
        this.activeEffects = new HashMap<>();
        this.clearOnDeath = new HashSet<>();
        this.haveTasks = Collections.newSetFromMap(new WeakHashMap<>());

        active = true;
        dead = entity.isDead();
    }

    @Override
    public UUID getUniqueId() {
        return uid;
    }

    @Override
    public LivingEntity getLivingEntity() {
        return entity.get();
    }

    @Override
    public boolean isActive() {
        if (active && entity.get() == null) {
            onInvalidated();
        }
        return active;
    }

    @Override
    public boolean isPlayer() {
        return isPlayer;
    }

    @Override
    public CombatEntity getCombatEntity() {
        return this;
    }

    @Override
    public EntityStats getStats() {
        return this;
    }

    @Override
    public EntityStatusEffects getStatusEffects() {
        return this;
    }

    @Override
    public EntityThreat getThreat() {
        return threat;
    }

    @Override
    public boolean addStatusEffect(StatusEffectType type, long durationTicks) {
        if (type == null || durationTicks < 1 || entity.get() == null || dead) {
            return false;
        }
        StatusEffectTask currentEffect = activeEffects.get(type);
        long expiry = getMillisecondExpiry(durationTicks);

        // effect of same type already present
        if (currentEffect != null) {

            if (currentEffect.expiry > expiry) {
                // if the current effect of the same type has a longer duration, returns
                return false;

            } else {
                // else cancel the current effect's expiration
                currentEffect.cancel();
            }
        }

        // if overriding previous effect, no need to add the effect anew.
        StatusEffectTask newEffect =
                new StatusEffectTask(this, durationTicks, type, currentEffect == null);
        activeEffects.put(type, newEffect);
        if (ui != null) {
            ui.onStatusEffectUpdate(type, expiry);
        }
        return true;
    }

    @Override
    public boolean removeStatusEffect(StatusEffectType type) {
        StatusEffectTask currentEffect = activeEffects.remove(type);
        if (currentEffect == null) {
            return false;
        }
        currentEffect.remove();
        return true;
    }

    @Override
    public boolean hasStatusEffect(StatusEffectType type) {
        StatusEffectTask task = activeEffects.get(type);
        return task != null && task.active;
    }

    @Override
    public long getStatusEffectExpiry(StatusEffectType type) {
        StatusEffectTask task = activeEffects.get(type);
        if (task == null) {
            return 0;
        }
        return task.expiry;
    }

    @Override
    public Set<StatusEffectType> getActiveStatusEffects() {
        return new HashSet<>(activeEffects.keySet());
    }

    @Override
    public boolean canUseSkills() {
        for (StatusEffectType type : activeEffects.keySet()) {
            if (type.blocksSkillsAndSpells()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double getRegeneratingStat(RegeneratingStat stat) {
        RegeneratingStatValue ret = regeneratingStats.get(stat);
        if (ret == null) {
            return 0;
        }
        return ret.getCurrentValue();
    }

    @Override
    public double getStat(Stat stat) {
        StatValue ret = stats.get(stat);
        if (ret == null) {
            return 0;
        }
        return ret.getFinalValue();
    }

    @Override
    public void editRegeneratingStat(RegeneratingStat stat, double modifier) {
        if (stat == null) {
            return;
        }
        RegeneratingStatValue value = getRegeneratingStatValue(stat);
        value.editValue(modifier);
    }

    @Override
    public void setRegeneratingStat(RegeneratingStat stat, double setTo) {
        if (stat == null) {
            return;
        }
        RegeneratingStatValue value = getRegeneratingStatValue(stat);
        value.setValue(setTo);
    }

    @Override
    public ValueModifierBuilder newStatModifierBuilder(Stat modify) throws IllegalArgumentException {
        if (modify == null) {
            throw new IllegalArgumentException("stat cannot be null");
        }
        StatValue value = getStatValue(modify);
        return new ValueModifierBuilder(value, this);
    }

    @Override
    public void onConstruction(ValueModifier newModifier) {
        if (newModifier.isRemovedOnDeath()) {
            clearOnDeath.add(newModifier);
        }
        if (newModifier.isRemovedOnDeath() || newModifier.getExpiration() < Long.MAX_VALUE
                || newModifier instanceof DecayingValueModifier) {
            haveTasks.add(newModifier);
        }
    }

    /**
     * Sets the player user interface that should be notified in changes in this combat data.
     * @param ui The player combat interface to notify of changes. Can be <code>null</code> to clear
     *           any previous interface.
     */
    public void setPlayerCombatInterface(PlayerCombatInterface ui) {
        this.ui = ui;
    }

    /**
     * Called when this object's entity takes damage.
     */
    void onDamage() {
        // creates a new collection to iterate over effects while removing them
        for (StatusEffectType effect : new ArrayList<>(activeEffects.keySet())) {
            // removes status effects that are removed upon taking damage.
            if (effect.isRemovedOnDamage()) {
                removeStatusEffect(effect);
            }
        }
    }

    /**
     * Called when this object's entity dies.
     */
    void onDeath() {
        dead = true;
        // creates a new collection to iterate over effects while removing them
        for (StatusEffectType effect : new ArrayList<>(activeEffects.keySet())) {
            removeStatusEffect(effect);
        }
        for (ValueModifier modifier : clearOnDeath) {
            modifier.remove();
        }
        clearOnDeath.clear();
    }

    /**
     * Called when this object's entity respawns (Player only).
     */
    void onRespawn() {
        dead = false;
    }

    /**
     * Called when this object becomes invalid and inactive.
     */
    void onInvalidated() {
        if (active) {
            active = false;

            // cancels any tasks that are ongoing.
            for (ValueModifier modifier : haveTasks) {
                if (modifier != null) {
                    modifier.remove();
                }
            }
            haveTasks.clear();

            // creates a new collection to iterate over effects while removing them
            for (StatusEffectType effect : new ArrayList<>(activeEffects.keySet())) {
                removeStatusEffect(effect);
            }

            if (threat != null) {
                threat.onInvalidated();
            }

            if (ui != null)
                ui.onInvalidated();
        }
    }

    /**
     * Refreshes the <code>LivingEntity</code> object wrapped by this combat data.
     * @param entity The currently valid <code>LivingEntity</code> for this combat data.
     * @throws IllegalArgumentException On a <code>null</code> entity object or one that is not
     *                                  associated with this combat data (has a different unique name).
     */
    void refreshEntity(LivingEntity entity) throws IllegalArgumentException {
        if (entity == null || !entity.getUniqueId().equals(uid)) {
            throw new IllegalArgumentException(
                    "The entity provided is not a refreshed version of this combat entity's original living entity");
        }
        if (entity.isValid()) {
            this.entity = new WeakReference<>(entity);
        }
    }

    private long getMillisecondExpiry(long durationTicksFromNow) {
        return System.currentTimeMillis() + (durationTicksFromNow * 50);
    }

    private StatValue getStatValue(Stat stat) {
        StatValue value = stats.get(stat);
        if (value == null) {
            value = new StatValue(stat, 0, listener);
            stats.put(stat, value);
        }
        return value;
    }

    private RegeneratingStatValue getRegeneratingStatValue(RegeneratingStat stat) {
        RegeneratingStatValue value = regeneratingStats.get(stat);
        if (value == null) {
            value = new RegeneratingStatValue(stat, getStatValue(stat.getMaxStat()), 0, listener);
            regeneratingStats.put(stat, value);
        }
        return value;
    }

    /**
     * Listens to and passes along changes in stats.
     */
    private class ChangeListener implements StatChangeListener, RegeneratingStatChangeListener {

        private CombatEntity outer;

        private ChangeListener(CombatEntity outer) {
            this.outer = outer;
        }

        @Override
        public void onRegeneratingStatChange(RegeneratingStat changed, double newValue, double oldValue) {
            if (changed == RegeneratingStat.HEALTH) {
                healthHandler.onHealthChange(outer, newValue, oldValue);
            }
            if (ui != null) {
                ui.onRegeneratingStatChange(changed, newValue, oldValue);
            }
        }

        @Override
        public void onStatChange(Stat changed, double newValue, double oldValue) {
            RegeneratingStat maxOf = RegeneratingStat.getFromMax(changed);
            if (maxOf != null) {
                RegeneratingStatValue value = regeneratingStats.get(maxOf);
                if (value != null) {
                    value.checkValue();
                }
            }

            if (changed == Stat.MAX_HEALTH) {
                healthHandler.onMaxHealthChange(outer, newValue, oldValue);

            } else if (changed == Stat.SPEED) {
                speedEngine.onSpeedChange(outer, newValue);
            }

            if (ui != null) {
                ui.onStatChange(changed, newValue, oldValue);
            }
        }

    }

    /**
     * A task that limits the duration of status effects, as well as updating a user interface when
     * the duration changes.
     */
    private class StatusEffectTask extends BukkitRunnable {

        private CombatEntity outer;
        private final long expiry;
        private final StatusEffectType type;

        private boolean interrupted;
        private boolean active;

        private StatusEffectTask(CombatEntity outer, long durationTicks, StatusEffectType type,
                                 boolean apply) {
            this.outer = outer;
            expiry = getMillisecondExpiry(durationTicks);
            this.type = type;

            if (apply) {
                type.apply(outer);
            }
            active = true;

            Bukkit.getServer().getPluginManager()
                    .callEvent(new CombatEngineStatusEffectAddedEvent(outer, type, durationTicks));

            runTaskLater(LegendsOfValeros.getInstance(), durationTicks);
        }

        @Override
        public void run() {
            if (active) {
                cancel();
                active = false;
                type.remove(outer);
                activeEffects.remove(type);

                RemovalReason reason = interrupted ? RemovalReason.INTERRUPTED : RemovalReason.EXPIRED;

                if (ui != null) {
                    ui.onStatusEffectRemoved(type, reason);
                }

                if (isActive()) {
                    Bukkit.getServer().getPluginManager()
                            .callEvent(new CombatEngineStatusEffectRemovedEvent(outer, type, reason));
                }
            }
        }

        private void remove() {
            interrupted = true;
            run();
        }

    }

}
