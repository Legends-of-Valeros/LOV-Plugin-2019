package com.legendsofvaleros.modules.combatengine.statuseffects;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reduces an entity's vision and hit chance.
 */
public class NightVision {

  private static final PotionEffectType TYPE = PotionEffectType.NIGHT_VISION;
  private static final PotionEffect EFFECT = new PotionEffect(TYPE, Integer.MAX_VALUE, 0);

  private static Map<UUID, ValueModifier> MODS = new HashMap<>();

  static {
    LogoutListener listener = new LogoutListener();
    CombatEngine.getInstance().registerEvents(listener);
  }

  public static void apply(CombatEntity entity) {
    remove(entity);

    LivingEntity le = entity.getLivingEntity();
    if (le != null) {
      le.removePotionEffect(TYPE);
      le.addPotionEffect(EFFECT);
    }
  }

  public static void remove(CombatEntity entity) {
    ValueModifier mod = MODS.remove(entity.getUniqueId());
    if (mod != null) {
      mod.remove();
      LivingEntity le = entity.getLivingEntity();
      if (le != null) {
        le.removePotionEffect(TYPE);
      }
    }
  }

  /**
   * Makes sure that the blindness potion effect does not persist if a player logs out while they
   * are blinded.
   */
  private static class LogoutListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
      event.getPlayer().removePotionEffect(TYPE);
    }
  }

}
