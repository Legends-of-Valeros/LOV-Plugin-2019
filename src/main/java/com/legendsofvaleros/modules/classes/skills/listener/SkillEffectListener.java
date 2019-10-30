package com.legendsofvaleros.modules.classes.skills.listener;

import com.legendsofvaleros.modules.classes.skills.skilleffect.RemovalReason;
import com.legendsofvaleros.modules.classes.skills.skilleffect.SkillEffect;

/**
 * Informed of changes in effects from skills/spells on an individual player-charater.
 * 
 * @see SkillEffect
 */
public interface SkillEffectListener {

  /**
   * Called when an effect from a skill/spell is added or updated for the player-character.
   * <p>
   * Not called if a status effect is added that does not actually overwrite a previously active
   * instance of the effect. This can happen for any reason, but examples might be if the new effect
   * has a shorter duration or is a lower level than the preexisting effect.
   * 
   * @param effect The effect that was added or updated.
   * @param expiry A millisecond timestamp of when the status effect will expire (some point in the
   *        future).
   * @param effectLevel The level of the updated effect.
   */
  void onSkillEffectUpdate(SkillEffect<?> effect, long expiry, int effectLevel);

  /**
   * Called when an effect from a skill/spell is removed from or expires for the player-character.
   * <p>
   * Not called if an effect is removed just in order to replace it with another instance of the
   * same effect.
   * 
   * @param removed The effect that is being removed or expiring.
   * @param effectLevel The level of the effect that is being removed or expiring.
   * @param reason The reason the effect was removed.
   */
  void onSkillEffectRemoved(SkillEffect<?> removed, int effectLevel,
                            RemovalReason reason);

}
