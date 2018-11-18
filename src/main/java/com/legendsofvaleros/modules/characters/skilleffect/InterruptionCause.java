package com.legendsofvaleros.modules.characters.skilleffect;

/**
 * Common and natively supported ways in which skill/spell-effects can be interrupted.
 */
public enum InterruptionCause {

  /**
   * Effect will be interrupted when the player-character takes damage.
   */
  TAKE_DAMAGE(),

  /**
   * Effect will be interrupted when the player-character causes/deals damage to another entity.
   */
  CAUSE_DAMAGE(),

  /**
   * Effect will be interrupted when the player-character dies.
   */
  DEATH(),

  /**
   * Effect will be interrupted when the player-character logs out. Causes an effect never to
   * persist across logins.
   */
  LOGOUT()

}
