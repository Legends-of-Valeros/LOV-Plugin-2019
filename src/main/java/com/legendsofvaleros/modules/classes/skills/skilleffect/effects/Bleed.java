package com.legendsofvaleros.modules.classes.skills.skilleffect.effects;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterCombatLogoutEvent;
import com.legendsofvaleros.modules.classes.skills.skilleffect.InterruptionCause;
import com.legendsofvaleros.modules.classes.skills.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.classes.skills.skilleffect.PersistingEffect;
import com.legendsofvaleros.modules.classes.skills.skilleffect.PersistingEffect.PersistingEffectBuilder;
import com.legendsofvaleros.modules.classes.skills.skilleffect.RemovalReason;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.RomanNumeral;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * A poison that deals damage as a simple percentage of max health.
 */
public class Bleed extends DamageOverTime<Void> {

  private static final long TICK_RATE = 40;
  public static final double PERCENT_PER_LEVEL = 0.25;

  private static final String BASE_UI_NAME = "Bleed";
  private static final String TICK_RATE_STR = "2 seconds";

  private final BukkitRunnable damageTask;

  public Bleed() throws IllegalArgumentException {
    super("Bleed", 1, Integer.MAX_VALUE, InterruptionCause.DEATH);

    damageTask = new BukkitRunnable() {
      @Override
      public void run() {
        CombatEngine eng = CombatEngine.getInstance();
        for (MetaEffectInstance<Void> effectInstance : getAllInstances()) {
          CombatEntity ce = eng.getCombatEntity(effectInstance.getAffected());
          if (ce != null) {
            EntityStats stats = ce.getStats();
            double percentage = effectInstance.getLevel() * PERCENT_PER_LEVEL;
            double damage = percentage * stats.getStat(Stat.MAX_HEALTH);

            eng.causeTrueDamage(effectInstance.getAffected(), sanitizeAppliedBy(effectInstance),
                damage, null);
          }
        }
      }
    };

    damageTask.runTaskTimer(LegendsOfValeros.getInstance(), TICK_RATE, TICK_RATE);
  }

  @Override
  public String generateUserFriendlyName(MetaEffectInstance<Void> effectInstance) {
    String ret = BASE_UI_NAME;
    if (effectInstance != null && effectInstance.getLevel() > 1) {
      ret += " " + RomanNumeral.convertToRoman(effectInstance.getLevel());
    }
    return ret;
  }

  @Override
  public String generateUserFriendlyDetails(MetaEffectInstance<Void> effectInstance) {
    if (effectInstance != null) {
      return "Deals bleed damage equal to "
          + (Math.round(100 * PERCENT_PER_LEVEL * effectInstance.getLevel()))
          + "% of max health every " + TICK_RATE_STR + ".";

    } else {
      return "Deals bleed damage equal to a % of max health every " + TICK_RATE_STR + ".";
    }
  }

  @Override
  protected boolean onApply(LivingEntity applyTo, MetaEffectInstance<Void> effectInstance, MetaEffectInstance<Void> replaced, long durationMillis) {

    if (replaced != null) {
      // does not overwrite a previous instance if the new instance is lower level or is the same
      // level but has a shorter duration
      return effectInstance.getLevel() >= replaced.getLevel()
          && (effectInstance.getLevel() != replaced.getLevel() || effectInstance
          .getRemainingDurationMillis() > replaced.getRemainingDurationMillis());
    }

    return true;
  }

  @Override
  protected boolean onReapply(PlayerCharacter reapplyTo, MetaEffectInstance<Void> effectInstance, PersistingEffect persistedRecord) {
    return true;
  }

  @Override
  protected void onRemove(UUID entityId, LivingEntity removeFrom, MetaEffectInstance<Void> effectInstance, RemovalReason reason) {
    throw new NotImplementedException();
  }

  @Override
  protected long getDefaultDurationMillis(LivingEntity affected, int effectLevel) {
    // arbitrary value. No inherent default
    return 10000;
  }

  @Override
  protected boolean onPersist(PlayerCharacter persistingFor, MetaEffectInstance<Void> effectInstance, PersistingEffectBuilder builder) {
    return true;
  }

  @Override
  protected void onCombatLog(Player appliedBy, MetaEffectInstance<Void> effectInstance, PlayerCharacterCombatLogoutEvent event) {
    CombatEntity ce = CombatEngine.getEntity(effectInstance.getAffected());
    long remainingHits = effectInstance.getRemainingDurationMillis() / (TICK_RATE * 50);
    double percentage = effectInstance.getLevel() * PERCENT_PER_LEVEL;

    // applies the remaining poison immediately on pvp combat logging
    double damage = remainingHits * (percentage * ce.getStats().getStat(Stat.MAX_HEALTH));
    CombatEngine.getInstance().causeTrueDamage(effectInstance.getAffected(), appliedBy,
        damage, null);

    // removes this so it doesnt persist in addition to dealing all damage immediately
    remove(event.getPlayer());
  }
}
