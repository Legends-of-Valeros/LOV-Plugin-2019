package com.legendsofvaleros.modules.characters.skilleffect.effects;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.core.LoginTime;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterCombatLogoutEvent;
import com.legendsofvaleros.modules.characters.skilleffect.InterruptionCause;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.characters.skilleffect.SkillEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A skill effect that applies damage over time.
 * <p>
 * Intended not to allow PvP damage to persist across logins. Instead, allows subclasses to apply
 * all damage on logout to ensure that players cannot log out to rob their killers of DoT
 * damage/kill credit.
 */
public abstract class DamageOverTime<T> extends SkillEffect<T> {

  public DamageOverTime(String id, int minLevel, int maxLevel,
      InterruptionCause... interruptionCauses) throws IllegalArgumentException {
    super(id, minLevel, maxLevel, false, interruptionCauses);
    Characters.getInstance().registerEvents(new CombatLoggingListener());
  }

  /**
   * Called when a player affected by this effect logs out of combat, only if the effect was applied
   * by another player whose credit for the damage is important.
   * <p>
   * Only called if:
   * <ol>
   * <li>A player logs out of combat,
   * <li>The combat-logging player is affected by this, and,
   * <li>The effect in question was applied by a player-character who is still logged in and needs
   * credit for the damage.
   * </ol>
   * <p>
   * Called as soon as possible in the logout process in order to preempt things like data saving
   * and have any actions taken as a result of this effect persist across logins.
   * 
   * @param appliedBy The player that applied this effect and needs to be credited for it.
   * @param effectInstance The instance of this effect that was affecting the player that logged out
   *        during combat.
   * @param event The event thrown when the player logged out during combat.
   */
  protected abstract void onCombatLog(Player appliedBy, MetaEffectInstance<T> effectInstance,
      PlayerCharacterCombatLogoutEvent event);

  /**
   * Sanitizes who the effect was applied by based on their current status.
   * <p>
   * For example, returns <code>null</code> if this effect was applied by a player who has since
   * logged off or switched characters and should no longer receive credit for damage over time.
   * 
   * @param effectInstance The instance of this effect to get a sanitized applied-by entity from.
   * @return A reference to the entity that applied this damage-over-time effect, if one exists.
   *         Else <code>null</code>.
   */
  protected final LivingEntity sanitizeAppliedBy(MetaEffectInstance<T> effectInstance) {
    if (effectInstance == null || effectInstance.getAppliedBy() == null) {
      return null;
    }

    // only rule currently implemented is not to use a player if they have logged off or switched
    // characters since applying this effect
    if (effectInstance.getAppliedByCharacterId() != null) {
      Player player = (Player) effectInstance.getAppliedBy();
      if (player == null || !player.isOnline()) {
        return null;
      }

      PlayerCharacter current = Characters.getPlayerCharacter(player);
      long appliedAt = System.currentTimeMillis() - effectInstance.getElapsedDurationMillis();
      // denies credit to a player who applied the effect if they have logged off, switched to
      // another character, or if they have relogged on the same character since the effect was
      // applied.
      if (current == null
          || !current.getUniqueCharacterId().equals(effectInstance.getAppliedByCharacterId())
          || appliedAt < LoginTime.getLastLogin(current)) {
        return null;
      }
    }

    return effectInstance.getAppliedBy();
  }

  /**
   * Listens for players logging out of combat while affected by this.
   */
  private class CombatLoggingListener implements Listener {

    @EventHandler
    public void onPlayerCharacterCombatLogout(PlayerCharacterCombatLogoutEvent event) {
      @SuppressWarnings("unchecked")
      MetaEffectInstance<T> effectInstance =
          (MetaEffectInstance<T>) getEntityInstance(event.getPlayer());

      LivingEntity appliedBy = sanitizeAppliedBy(effectInstance);
      if (appliedBy != null && appliedBy.getType() == EntityType.PLAYER
          && !appliedBy.equals(event.getPlayer())) {
        onCombatLog((Player) appliedBy, effectInstance, event);
      }
    }

  }

}
