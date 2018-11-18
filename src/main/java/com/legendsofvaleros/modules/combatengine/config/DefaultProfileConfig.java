package com.legendsofvaleros.modules.combatengine.config;

import com.legendsofvaleros.modules.combatengine.core.CombatProfile;

/**
 * Defines the default stats for entities if no stats are provided when they are initialized.
 */
public interface DefaultProfileConfig {

  /**
   * Gets the default profile that is copied from for an entity's based stats when no other profile
   * is provided.
   * 
   * @return The default stat profile.
   */
  CombatProfile getDefaultProfile();

}
