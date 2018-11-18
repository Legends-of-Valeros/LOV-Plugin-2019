package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.config.SpeedConfig;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Implements speed and slow effects for entities' speed stats.
 * <p>
 * Uses speed/slow potion effects.
 */
public class SpeedEngine {

    // if speed is this or anything lower, the entity should be essentially immobile.
    // does NOT ensure that the entity does not move, just makes normal movement hard
    private static final double IMMOBILE_SPEED_STAT = 0;
    private static final int IMMOBILE_SLOW_AMPLIFIER = 7;
    private static final int IMMOBILE_JUMP_AMPLIFIER = -5;

    private SpeedConfig config;

    public SpeedEngine(SpeedConfig config) {
        this.config = config;
        LegendsOfValeros.getInstance().getServer().getPluginManager().registerEvents(new SpeedListener(), LegendsOfValeros.getInstance());
    }

    void onSpeedChange(CombatEntity entity, double newSpeed) {
        refreshSpeed(entity, newSpeed);
    }

    private void refreshSpeed(CombatEntity entity, double newSpeed) {
        if (entity == null) {
            return;
        }
        LivingEntity le = entity.getLivingEntity();

        if (entity.isActive() && le != null) {
            double difFromNormal = newSpeed - config.getNormalSpeed();

            // negative for slow, positive for speed. Relies on integer division, meaning that if there is
            // 10 speed points per potion level, and the entity has 16 more speed than normal, they will
            // get speed 1, not 2. You have to have at least speedPointsPerLevel * potionLevel speed
            // points above normal to get a given potion level; it is not rounded up ever.
            int potionLevel = (int) (difFromNormal / config.getSpeedPointsPerPotionLevel());
            int previousPotionLevel = getPotionLevel(le);

            if (potionLevel != previousPotionLevel) {
                le.removePotionEffect(PotionEffectType.SPEED);
                le.removePotionEffect(PotionEffectType.SLOW);
                le.removePotionEffect(PotionEffectType.JUMP);

                if (potionLevel > 0) { // faster than normal

                    // corrects for the fact that amplifiers start at 0
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE,
                            potionLevel - 1));

                } else if (newSpeed <= IMMOBILE_SPEED_STAT) { // immobility

                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE,
                            IMMOBILE_SLOW_AMPLIFIER));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE,
                            IMMOBILE_JUMP_AMPLIFIER));

                } else if (potionLevel < 0) { // run-of-the-mill slowness

                    // corrects for the fact that amplifiers start at 0
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE,
                            (-1 * potionLevel) - 1));
                }
            }
        }
    }

    private int getPotionLevel(LivingEntity entity) {
        for (PotionEffect effect : entity.getActivePotionEffects()) {

            if (effect.getType().equals(PotionEffectType.SLOW)) {
                // corrects for the fact that amplifiers start at 0
                return (-1 * effect.getAmplifier()) - 1;

            } else if (effect.getType().equals(PotionEffectType.SPEED)) {
                return effect.getAmplifier() + 1;
            }
        }

        return 0;
    }

    /**
     * Initiates speed settings.
     */
    private class SpeedListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onCombatEntityCreate(CombatEntityCreateEvent event) {
            refreshSpeed(event.getCombatEntity(), event.getCombatEntity().getStats().getStat(Stat.SPEED));
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerRespawn(PlayerRespawnEvent event) {
            CombatEntity ce = CombatEngine.getEntity(event.getPlayer());
            if (ce != null) {
                refreshSpeed(ce, ce.getStats().getStat(Stat.SPEED));
            }
        }

    }

}
