package com.legendsofvaleros.modules.combatengine.ui;

import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;

/**
 * A user interface for players to be informed of changes in combat stats.
 */
public interface PlayerCombatInterface extends StatChangeListener, RegeneratingStatChangeListener {

  /**
   * Called when a status effect is added or extended for the entity.
   * <p>
   * Not called if a status effect is added that does not actually change the status of the entity
   * in any way, such as if a stun is applied but the player is already stunned for longer than the
   * stun being added.
   * 
   * @param type The type of effect that is added or extended.
   * @param expiry When the status effect will expire.
   */
  void onStatusEffectUpdate(StatusEffectType type, long expiry);

  /**
   * Called when a status effect is removed from or expires for the entity.
   * 
   * @param type The type of status effect that is being removed or expiring.
   */
  void onStatusEffectRemoved(StatusEffectType type, StatusEffectType.RemovalReason reason);
  
  void onInvalidated();

}
