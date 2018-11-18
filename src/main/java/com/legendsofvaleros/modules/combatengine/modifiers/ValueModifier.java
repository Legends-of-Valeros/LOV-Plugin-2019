package com.legendsofvaleros.modules.combatengine.modifiers;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A modifier for a modifiable double value.
 */
public abstract class ValueModifier {

    private final ValueModifierBuilder.ModifierType type;
    protected ModifiableDouble valueModifying;
    protected double currentModifier;

    private boolean removeOnDeath;
    private ExpireTask expireTask;

    private boolean active;

    /**
     * Class constructor that creates a permanent modifier.
     * @param modify        The specific value being modified.
     * @param type          The type of modifier this should be.
     * @param startingValue How much to modify the value by.
     * @param removeOnDeath <code>true</code> if this should be removed when the entity dies.
     */
    ValueModifier(ModifiableDouble modify, ValueModifierBuilder.ModifierType type, double startingValue,
                  boolean removeOnDeath) {
        if (modify == null || type == null) {
            throw new IllegalArgumentException("params cannot be null!");
        }
        this.valueModifying = modify;
        this.type = type;
        this.currentModifier = startingValue;
        this.removeOnDeath = removeOnDeath;
        this.active = true;

        switch (type) {
            case FLAT_EDIT:
                valueModifying.flatEdit(currentModifier, false);
                break;

            case FLAT_EDIT_IGNORES_MULTIPLIERS:
                valueModifying.flatEdit(currentModifier, true);
                break;

            case MULTIPLIER:
                valueModifying.addMultiplier(currentModifier);
                break;
        }
    }

    /**
     * Class constructor that creates a modifier that will expire after a defined amount of time.
     * @param modify        The specific value being modified.
     * @param type          The type of modifier this should be.
     * @param startingValue How much to modify the value by.
     * @param removeOnDeath <code>true</code> if this should be removed when the entity dies.
     * @param removeOnDeath <code>true</code> if this should be removed when the entity dies.
     */
    ValueModifier(ModifiableDouble modify, ValueModifierBuilder.ModifierType type, double startingValue,
                  boolean removeOnDeath, long expireAfterTicks) {
        this(modify, type, startingValue, removeOnDeath);
        this.expireTask = new ExpireTask(expireAfterTicks);
    }

    /**
     * Gets whether this modifier is currently active or has been removed.
     * <p>
     * Once a modifier has been removed, it does nothing. To reapply it, create a new effect.
     * @return <code>true</code> if the effect is still active and has not yet been removed or
     * expired.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Cancels and removes this modifier.
     * <p>
     * Renders this modifier useless. Cannot be reversed; instead, create a new modifier. Does nothing
     * if this has already expired or been removed.
     */
    public void remove() {
        if (active) {
            active = false;

            switch (type) {
                case FLAT_EDIT:
                    valueModifying.flatEdit(-1 * currentModifier, false);
                    break;

                case FLAT_EDIT_IGNORES_MULTIPLIERS:
                    valueModifying.flatEdit(-1 * currentModifier, true);
                    break;

                case MULTIPLIER:
                    valueModifying.removeMultiplier(currentModifier);
                    break;
            }

            if (expireTask != null) {
                expireTask.cancel();
            }
            onRemove();
        }
    }

    /**
     * Gets the current value of this modifier.
     * <p>
     * This value may change over time. It is not safe to cache it.
     * @return This modifier's current value.
     */
    public double getValue() {
        return currentModifier;
    }

    /**
     * Gets the type of modifier that this is.
     * @return This modifier's type. How it alters the modified value.
     */
    public ValueModifierBuilder.ModifierType getType() {
        return type;
    }

    /**
     * Gets the expiration of this modifier as a timestamp, if there is one.
     * @return This modifier's expiration. <code>Long.MAX_VALUE</code> if it is not set to expire.
     */
    public long getExpiration() {
        if (expireTask != null) {
            return expireTask.expiry;
        }
        return Long.MAX_VALUE;
    }

    /**
     * Gets whether this modifier will be/has been removed the next time the entity dies after this is
     * applied.
     * @return <code>true</code> if this modifier does not survive death, else <code>false</code>.
     */
    public boolean isRemovedOnDeath() {
        return removeOnDeath;
    }

    /**
     * Called when this modifier is removed.
     */
    protected abstract void onRemove();

    /**
     * Expires the modifier after a configured amount of time.
     */
    private class ExpireTask extends BukkitRunnable {

        private long expiry;

        private ExpireTask(long durationTicks) {
            this.expiry = System.currentTimeMillis() + (durationTicks * 50);
            runTaskLater(LegendsOfValeros.getInstance(), durationTicks);
        }

        @Override
        public void run() {
            remove();
        }

    }

}
