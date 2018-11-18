package com.legendsofvaleros.modules.characters.skilleffect;

/**
 * Reasons for a skill/spell effect could be removed.
 */
public enum RemovalReason {

  /**
   * The effect reached the end of its lifetime and expired naturally.
   */
  EXPIRED(),

  /**
   * The effect was stopped prematurely either by one of the natively supported
   * <code>InterruptionCause</code>s or by a client manually removing the effect.
   */
  INTERRUPTED(),

  /**
   * The effect was replaced by another instance of the same effect.
   */
  REPLACED(),

  /**
   * An affected player-character logged out. In this case, the effect may be readded when they log
   * back in, but it was removed locally for now.
   * <p>
   * Useful mainly for any necessary cleanup. Does not represent and actual end to the effect,
   * except as it exists locally for the current session of the player-character.
   */
  LOGOUT(),

  /**
   * An affected non-player entity was invalidated and removed from memory.
   */
  INVALIDATED(),

  /**
   * Some other reason it was removed. Should be displayed in user interfaces with some vague
   * language, where appropriate.
   */
  OTHER(),

  /**
   * Some other reason it was removed, but one that should not be displayed in any user interfaces.
   */
  OTHER_DO_NOT_DISPLAY()

}
