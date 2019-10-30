package com.legendsofvaleros.modules.classes.skills.skilleffect.effects;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.classes.skills.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.classes.skills.skilleffect.PersistingEffect;
import com.legendsofvaleros.modules.classes.skills.skilleffect.PersistingEffect.PersistingEffectBuilder;
import com.legendsofvaleros.modules.classes.skills.skilleffect.RemovalReason;
import com.legendsofvaleros.modules.classes.skills.skilleffect.SkillEffect;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * A persistent wrapper for a CombatEngine status effect.
 */
public class PersistingStatusEffect extends SkillEffect<Void> {

  // TODO will doNotCycle work with multiple players/mobs in combat simultaneously? Is the
  // problematic loop that it blocks generally uninterruptible?

  private StatusEffectType type;
  // prevents cycling by not applying the same effect twice
  private UUID doNotCycle;

  public PersistingStatusEffect(StatusEffectType type) throws IllegalArgumentException {
    // no skill-effect interruptions, lets CombatEngine handle interruptions
    super(type.name(), 1, 1, type.isGood());
    this.type = type;
  }

  @Override
  public String generateUserFriendlyName(MetaEffectInstance<Void> effectInstance) {
    return type.getUserFriendlyName();
  }

  @Override
  public String generateUserFriendlyDetails(MetaEffectInstance<Void> effectInstance) {
    return type.getUserFriendlyDescription();
  }

  @Override
  protected boolean onApply(LivingEntity applyTo, MetaEffectInstance<Void> effectInstance,
      MetaEffectInstance<Void> replaced, long durationMillis) {

    if (applyTo.getUniqueId().equals(doNotCycle)) {
      clearDoNotCycle();
      return false;
    }

    clearDoNotCycle();

    // does nothing because CombatEngine is already taking care of it
    return true;
  }

  @Override
  protected boolean onReapply(PlayerCharacter reapplyTo, MetaEffectInstance<Void> effectInstance,
      PersistingEffect persistedRecord) {
    doNotCycle = reapplyTo.getPlayerId();

    // reapplies the effect that was persisted from the previous login
    CombatEntity ce = CombatEngine.getEntity(reapplyTo.getPlayer());
    ce.getStatusEffects().addStatusEffect(type, effectInstance.getRemainingDurationMillis() / 50);

    return true;
  }

  @Override
  protected void onRemove(UUID entityId, LivingEntity removeFrom,
      MetaEffectInstance<Void> effectInstance, RemovalReason reason) {
    clearDoNotCycle();
    // does nothing because CombatEngine is already taking care of it
  }

  @Override
  protected long getDefaultDurationMillis(LivingEntity affected, int effectLevel) {
    // does nothing because CombatEngine is already taking care of it
    return 0;
  }

  @Override
  protected boolean onPersist(PlayerCharacter persistingFor,
      MetaEffectInstance<Void> effectInstance, PersistingEffectBuilder builder) {
    // no metadata to store
    return true;
  }

  private void clearDoNotCycle() {
    doNotCycle = null;
  }

}
