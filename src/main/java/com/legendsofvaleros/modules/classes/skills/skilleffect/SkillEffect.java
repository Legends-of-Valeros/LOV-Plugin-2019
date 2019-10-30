package com.legendsofvaleros.modules.classes.skills.skilleffect;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityInvalidatedEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * An effect on an entity from a skill or spell that has some duration that ticks down while they
 * play. Subclasses implement effects' behavior and logic.
 * <p>
 * Importantly, skill/spell effects support persistence across logins for player-characters. No
 * persistence of any kind is supported for non-players.
 * <p>
 * Attribution (who or what caused an effect) is <b>not</b> persisted, even for players. There are
 * too many logical issues. When a player logs out, whoever or whatever caused the effect is lost,
 * unless stored manually by the implementing effect.
 * 
 * @param <T> The type of meta that implementations can store with instances of this effect.
 */
public abstract class SkillEffect<T> {

  private final String effectId;
  private final boolean isGood;
  private final Set<InterruptionCause> interruptions;

  private final int minLevel;
  private final int maxLevel;

  private final Map<UUID, MetaEffectInstance<T>> affected;

  /**
   * Class constructor.
   * 
   * @param id The unique string name of this effect.
   * @param minLevel The minimum acceptable level of this effect. Level inputs will be sanitized
   *        based on this value. <code>Integer.MIN_VALUE</code> for no meaningful lower limit.
   * @param maxLevel The maximum acceptable level of this effect. Level inputs will be sanitized
   *        based on this value. <code>Integer.MAX_VALUE</code> for no meaningful upper limit.
   * @param interruptionCauses The things that can cause this effect to end early, if any.
   *        <code>null</code> if this should always last until it expires.
   * @throws IllegalArgumentException On a <code>null</code> or empty name, or one that is already
   *         used by another effect.
   */
  public SkillEffect(String id, int minLevel, int maxLevel, boolean isGood, InterruptionCause... interruptionCauses)
      throws IllegalArgumentException {

    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("name cannot be null or empty");
    }
    this.effectId = id;

    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
    
    this.isGood = isGood;

    interruptions = new HashSet<>();
    if (interruptionCauses != null) {
      for (InterruptionCause cause : interruptionCauses) {
        if (cause != null) {
          interruptions.add(cause);
        }
      }
    }

    affected = new HashMap<>();

    Characters.getInstance().registerEvents(new EffectListener());
  }

  /**
   * Gets the string name of this character effect.
   * 
   * @return The string name of this character effect.
   */
  public final String getId() {
    return effectId;
  }

  /**
   * Gets a user-friendly string of this effect's name, that can be used in user interfaces.
   * 
   * @param affectedEntity The entity affected by this effect, in order to get a context-sensitive
   *        name where appropriate. Can be <code>null</code> to get a general-purpose name for this
   *        effect with no context-sensitivity.
   * @return A user-friendly string of this effect's name.
   */
  public final String getUserFriendlyName(LivingEntity affectedEntity) {
    MetaEffectInstance<T> effectInstance = null;
    if (affectedEntity != null) {
      effectInstance = affected.get(affectedEntity.getUniqueId());
    }
    return generateUserFriendlyName(effectInstance);
  }

  /**
   * Generates a user-friendly name for this effect.
   * 
   * @param effectInstance The instance of the effect that a name was requested for.
   *        <code>null</code> if no instance in particular was provided, and a general-purpose name
   *        should be given.
   * @return A user-friendly name for this effect.
   */
  public abstract String generateUserFriendlyName(MetaEffectInstance<T> effectInstance);

  /**
   * Gets a user-friendly list of strings that detail this effect and what it does.
   * 
   * @param affectedEntity The entity affected by this effect, in order to get a context-sensitive
   *        list of details where appropriate. Can be <code>null</code> to get a general-purpose
   *        list of details for this effect with no context-sensitivity.
   * @return A user-friendly list of strings that detail this effect.
   */
  public final String getUserFriendlyDetails(LivingEntity affectedEntity) {
    MetaEffectInstance<T> effectInstance = null;
    if (affectedEntity != null) {
      effectInstance = affected.get(affectedEntity.getUniqueId());
    }
    return generateUserFriendlyDetails(effectInstance);
  }

  /**
   * Generates a user-friendly list of details about this effect.
   * 
   * @param effectInstance The instance of the effect that details were requested for.
   *        <code>null</code> if no instance in particular was provided, and a general-purpose list
   *        of details should be given.
   * @return A user-friendly list of detail(s) for this effect.
   */
  public abstract String generateUserFriendlyDetails(MetaEffectInstance<T> effectInstance);

  /**
   * Gets the minimum possible level of this effect.
   * <p>
   * Level inputs will be sanitized based on this value.
   * 
   * @return The minimum level of this effect. <code>Integer.MIN_VALUE</code> if none is defined.
   */
  public final int getMinLevel() {
    return minLevel;
  }

  /**
   * Gets the maximum possible level of this effect.
   * <p>
   * Level inputs will be sanitized based on this value.
   * 
   * @return The maximum level of this effect. <code>Integer.MAX_VALUE</code> if none is defined.
   */
  public final int getMaxLevel() {
    return maxLevel;
  }

  /**
   * Returns true if the effect is generally recognized as good.
   */
  public final boolean isGood() {
	  return isGood;
  }
  
  /**
   * Sanitizes a level input based on the min and max possible levels.
   * 
   * @param level The level to sanitize.
   * @return A sanitized version of the level that is as close as possible to the original input
   *         without being outside the range of min/max values.
   */
  protected final int sanitizeLevel(int level) {
    if (level < minLevel) {
      return minLevel;
    } else if (level > maxLevel) {
      return maxLevel;
    }
    return level;
  }

  /**
   * Gets whether an entity is currently affected by this.
   * <p>
   * For players, gets whether their currently logged in character (if any) is affected.
   * 
   * @param entity The entity to check whether they are affected by this.
   * @return <code>true</code> if the given entity is currently affected by this, else
   *         <code>false</code>.
   */
  public final boolean isAffected(LivingEntity entity) {
    if (entity == null) {
      return false;
    }
    return affected.containsKey(entity.getUniqueId());
  }

  /**
   * Gets the current per-entity instance of this effect, if the entity is currently affected by
   * this.
   * <p>
   * For players, gets whether the effect instance of their currently logged in character (if any).
   * 
   * @param entity The entity to get an individual instance of this effect for.
   * @return A per-entity instance of this effect for the given entity, if a current one is found.
   *         Else <code>null</code>.
   */
  public final SkillEffectInstance getEntityInstance(LivingEntity entity) {
    if (entity == null) {
      return null;
    }
    return affected.get(entity.getUniqueId());
  }

  /**
   * Gets a set of all of the currently applied instances of this effect.
   * <p>
   * Any edits to the collection will affect the underlying collection. Do not edit it unless you
   * know exactly what you are doing.
   * 
   * @return A live collection of all of the currently applied instances of this effect.
   */
  protected final Collection<MetaEffectInstance<T>> getAllInstances() {
    return affected.values();
  }

  /**
   * Attempts to apply this effect to an entity with a default duration.
   * <p>
   * May or may not successfully overwrite a previous instance of the effect, depending on the
   * specific arguments and the implementation of the effect.
   * 
   * @param applyTo The entity to apply this effect to.
   * @param appliedBy The entity this effect is being applied by, if any. Can be <code>null</code>
   *        if the effect is not from a specific entity or its source is ambiguous.
   * @param effectLevel The level of this effect to apply.
   * @return <code>true</code> if the applied affect was successfully added, <code>false</code> if
   *         it was blocked, such as if the previous instance of this effect was kept instead of
   *         being overridden.
   */
  public final boolean apply(LivingEntity applyTo, LivingEntity appliedBy, int effectLevel)
      throws IllegalStateException {
    if (applyTo == null) {
      return false;
    }
    return apply(applyTo, appliedBy, effectLevel, getDefaultDurationMillis(applyTo, effectLevel));
  }

  /**
   * Attempts to apply this effect to an entity with a specific millisecond duration.
   * <p>
   * May or may not successfully overwrite a previous instance of the effect, depending on the
   * specific arguments and the implementation of the effect. For example, some effects might not
   * let a lower-level instance of the effect override a higher-level instance.
   * 
   * @param applyTo The entity to apply this effect to.
   * @param appliedBy The entity this effect is being applied by, if any. Can be <code>null</code>
   *        if the effect is not from a specific entity or its source is ambiguous.
   * @param effectLevel The level of this effect to apply.
   * @param durationMillis The number of milliseconds this effect should last for.
   * @return <code>true</code> if the applied affect was successfully added, <code>false</code> if
   *         it was blocked, such as if the previous instance of this effect was kept instead of
   *         being overridden.
   * @throws IllegalArgumentException On a negative duration.
   */
  public final boolean apply(LivingEntity applyTo, LivingEntity appliedBy, int effectLevel,
      long durationMillis) throws IllegalStateException, IllegalArgumentException {
    if (applyTo == null) {
      return false;
    } else if (durationMillis < 0) {
      throw new IllegalArgumentException("duration cannot be null");
    }
    effectLevel = sanitizeLevel(effectLevel);

    MetaEffectInstance<T> effectInstance =
            new MetaEffectInstance<>(this, applyTo, appliedBy, effectLevel, durationMillis,
                    System.currentTimeMillis());

    MetaEffectInstance<T> replaced = affected.get(applyTo.getUniqueId());

    boolean finish = onApply(applyTo, effectInstance, replaced, durationMillis);

    if (finish) {
      if (replaced != null) {
        // does not tell ui the effect was removed because it is just being changed instead
        replaced.stop(RemovalReason.REPLACED);
      }

      effectInstance.scheduleTask(true);
      affected.put(applyTo.getUniqueId(), effectInstance);
    }

    return finish;
  }

  /**
   * Called when this effect is applied to an entity.
   * 
   * @param applyTo The entity the effect is being applied to.
   * @param effectInstance The per-entity instance of this effect.
   * @param replaced The per-entity instance of this effect that is being replaced by a new instance
   *        of this same effect. <code>null</code> if there was not a previously active instance to
   *        override.
   * @param durationMillis The number of milliseconds this effect will last for, unless it is
   *        interrupted or removed prematurely.
   * @return <code>true</code> if this effect should finish applying, <code>false</code> if it
   *         should be cancelled.
   */
  protected abstract boolean onApply(LivingEntity applyTo, MetaEffectInstance<T> effectInstance,
      MetaEffectInstance<T> replaced, long durationMillis);

  /**
   * Reapplies an instance of this effect that was stored across logins.
   * 
   * @param persistingEffect The persistent record of an instance of this effect.
   * @throws IllegalStateException If this effect is being reapplied for a player-character who is
   *         not currently logged in.
   */
  final void reapply(PersistingEffect persistingEffect) throws IllegalStateException {
    if (persistingEffect == null || !effectId.equals(persistingEffect.getEffectId())) {
      return;
    }

    CharacterId characterId = persistingEffect.getAffected();
    PlayerCharacter pc = Characters.getInstance().getCharacter(characterId);
    if (pc == null || !pc.isCurrent()) {
      throw new IllegalStateException("that player-character is not logged in");
    }

    long duration = persistingEffect.getRemainingDurationMillis();
    long started = System.currentTimeMillis();

    if (persistingEffect.getElapsedMillis() > 0) {
      // backdates the start and extends the total duration in line with a defined number of elapsed
      // milliseconds. Duration here ceases to be the remaining milliseconds, and becomes the total
      // duration, from the hypothetical point in the past.
      duration += persistingEffect.getElapsedMillis();
      started -= persistingEffect.getElapsedMillis();
    }

    int effectLevel = sanitizeLevel(persistingEffect.getLevel());

    MetaEffectInstance<T> effectInstance =
            new MetaEffectInstance<>(this, pc.getPlayer(), null, effectLevel, duration, started);

    boolean finish = onReapply(pc, effectInstance, persistingEffect);

    if (finish) {
      effectInstance.scheduleTask(false);
      affected.put(characterId.getPlayerId(), effectInstance);
    }
  }

  /**
   * Called when an effect instance is reapplied after having been loaded from a database record so
   * it could persist across logins.
   * 
   * @param reapplyTo The player-character the persisted effect is being reapplied to.
   * @param effectInstance The newly created local instance of the effect.
   * @param persistedRecord The persistent record of the effect.
   * @return <code>true</code> if this effect should finish reapplying, <code>false</code> if it
   *         should be cancelled.
   */
  protected abstract boolean onReapply(PlayerCharacter reapplyTo,
      MetaEffectInstance<T> effectInstance, PersistingEffect persistedRecord);

  /**
   * Prematurely interrupts and removes this effect from an entity.
   * 
   * @param removeFrom The entity to remove this effect from.
   */
  public final void remove(LivingEntity removeFrom) {
    remove((removeFrom != null ? removeFrom.getUniqueId() : null), removeFrom,
        RemovalReason.INTERRUPTED);
  }

  /**
   * Removes this effect from an entity.
   * 
   * @param entityId The name of the entity the effect is being removed from.
   * @param removeFrom The entity to remove this effect from, if it is still in memory. Else
   *        <code>null</code>.
   * @param reason The reason for the removal.
   */
  final void remove(UUID entityId, LivingEntity removeFrom, RemovalReason reason) {
    if (entityId != null) {
      MetaEffectInstance<T> effectInstance = affected.remove(entityId);
      if (effectInstance != null) {
        effectInstance.stop(reason);
        onRemove(entityId, removeFrom, effectInstance, reason);
      }
    }
  }

  /**
   * Called when this effect is removed from an entity.
   * <p>
   * <b>Not</b> called if the effect was replaced by another instance of this same effect. See
   * <p>
   * For players, this is called on player-character logout with the reason
   * <code>RemovalReason.LOGOUT</code>, even if it will be reapplied on the next login.
   * 
   * @param entityId The name of the entity the effect is being removed from.
   * @param removeFrom The entity the effect is being removed from, if it is still in memory. Else
   *        <code>null</code>.
   * @param effectInstance The per-entity instance of this effect that is being removed.
   * @param reason The reason the effect was removed.
   */
  protected abstract void onRemove(UUID entityId, LivingEntity removeFrom,
      MetaEffectInstance<T> effectInstance, RemovalReason reason);

  /**
   * Gets how long this effect should be applied for a given entity with a given level of this
   * effect.
   * 
   * @param affected The affected entity.
   * @param effectLevel The level of the effect being applied.
   * @return The default total duration, in milliseconds, that the instance of this effect should
   *         have.
   */
  protected abstract long getDefaultDurationMillis(LivingEntity affected, int effectLevel);

  /**
   * Called when an effect instance is being converted to a persistent database record so it can
   * persist across logins for a player-character.
   * 
   * @param persistingFor The player-character the effect is being stored for.
   * @param effectInstance The player-character-specific instance of this effect.
   * @param builder The builder of the persistent record, that can be used to store additional
   *        metadata with the persistent record.
   * @return <code>true</code> if the effect instance should be allowed to persist across logins.
   *         <code>false</code> if the persistence should be cancelled for this instance.
   * @see PersistingEffect.PersistingEffectBuilder
   */
  protected abstract boolean onPersist(PlayerCharacter persistingFor,
      MetaEffectInstance<T> effectInstance, PersistingEffect.PersistingEffectBuilder builder);

  /**
   * Listens to common player behaviors that might affect or interrupt effects.
   */
  private final class EffectListener implements Listener {

    // cleanup and persistence for players
    @EventHandler
    public void onPlayerCharacterLogout(PlayerCharacterLogoutEvent event) {
      PlayerCharacter pc = event.getPlayerCharacter();
      CharacterId characterId = pc.getUniqueCharacterId();

      MetaEffectInstance<T> effectInstance = affected.get(characterId.getPlayerId());
      remove(characterId.getPlayerId(), pc.getPlayer(), RemovalReason.LOGOUT);

      if (effectInstance != null && !interruptions.contains(InterruptionCause.LOGOUT)) {
        // saves the effect so it will persist across logins
        PersistingEffect.PersistingEffectBuilder builder =
            new PersistingEffect.PersistingEffectBuilder(effectId, characterId,
                effectInstance.getRemainingDurationMillis()).setLevel(effectInstance.getLevel())
                .setElapsedDurationMillis(effectInstance.getElapsedDurationMillis());

        if (onPersist(pc, effectInstance, builder))
          builder.buildAndSave();
      }
    }

    // cleanup for non-players
    @EventHandler
    public void onCombatEntityInvalidated(CombatEntityInvalidatedEvent event) {
      if (!event.getInvalidated().isPlayer()) {
        remove(event.getInvalidatedUuid(), event.getInvalidated().getLivingEntity(),
            RemovalReason.INVALIDATED);
      }
    }

    // on-damage interruptions
    @EventHandler
    public void onCombatEngineDamage(CombatEngineDamageEvent event) {
      if (!affected.isEmpty()) {

        if (interruptions.contains(InterruptionCause.TAKE_DAMAGE)) {
          remove(event.getDamaged().getLivingEntity());

        } else if (interruptions.contains(InterruptionCause.CAUSE_DAMAGE)
            && event.getAttacker() != null) {
          remove(event.getAttacker().getLivingEntity());
        }
      }
    }

    // on-death interruptions
    @EventHandler
    public void onCombatEngineDeath(CombatEngineDeathEvent event) {
      if (!affected.isEmpty() && interruptions.contains(InterruptionCause.DEATH)) {
        remove(event.getDied().getLivingEntity());
      }
    }
  }

}
