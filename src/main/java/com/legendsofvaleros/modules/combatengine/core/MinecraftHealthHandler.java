package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.damage.AttackStandIn;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityInvalidatedEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityPreCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.VanillaDamageCancelledEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.combatengine.stat.StatUtils;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.util.LoggingOut;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Logger;

/**
 * Maintains entity's vanilla MC health stats so that they are consistently proportional to their
 * health/max health CombatEngine stats.
 * <p>
 * Also prevents non-CombatEngine damage from taking place.
 * <p>
 * Modifies entities' vanilla health value when their CombatEngine health and max health stats
 * change.
 * <p>
 * When an entity takes damage, allows for the directionality of the damage to be set beforehand, in
 * order to allow for a flexible knockback from attacks. For example, this allows for things like
 * spells to knock back from the casting player, from the epicentre of an explosion, or cause no
 * knockback at all.
 */
public class MinecraftHealthHandler {

    // important: if dealing damage causes health to become NaN, this will cause a bug where the
    // player will be sort of dead, but still able to run around and their screen will shake nonstop.
    // In other words, a reasonable value (not Double.MAX_VALUE, for example) must be used to
    // definitively kill entities when necessary.
    private static final double FATAL_DAMAGE = 1000000;
    private static final List<PotionEffectType> BAD_POTIONS = Arrays.asList(PotionEffectType.ABSORPTION, PotionEffectType.HEALTH_BOOST);

    private Set<UUID> ignored;

    private AttackStandIn knockbackStandIn;
    private AttackStandIn noKnockbackStandIn;

    private Location nextDamageOrigin;

    public MinecraftHealthHandler() {
        this.ignored = new HashSet<>();

        // a living entity will cause knock back when it damages, even if its location is at the exact
        // same spot as the damaged entity. Instead uses two different entities, one that will cause
        // knockback and one that will not.
        knockbackStandIn = new AttackStandIn(EntityType.VILLAGER);

        noKnockbackStandIn = new AttackStandIn(EntityType.SNOWBALL);

        JavaPlugin plugin = LegendsOfValeros.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new DeathAnimationFixListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DamageListener(), plugin);
    }

    /**
     * Sets the location that the very next reduction in an entity's health should originate from.
     * <p>
     * The entity will be knocked back from the given location.
     * <p>
     * Should be called immediately and synchronously before the target entity's health is reduced,
     * with no chance of other damage being triggered in between the invocation of this method and the
     * health reduction taking place.
     * @param origin The location from which the next damaged entity should be knocked back. Can be
     *               <code>null</code> to make sure the next damaged entity does not get knocked back.
     */
    public void setNextDamageOrigin(Location origin) {
        nextDamageOrigin = origin;
    }

    void onHealthChange(CombatEntity entity, double newValue, double oldValue) {
        LivingEntity le = entity.getLivingEntity();
        if (le == null) {
            return;
        }

        double change = newValue - oldValue;

        if (change < 0) {
            damage(le, -1 * change, nextDamageOrigin);
            nextDamageOrigin = null;
        }

        // even if the health change was done via damage, still refreshes with exact, direct math to
        // ensure accuracy
        refreshHealth(entity);
    }

    void onMaxHealthChange(CombatEntity entity, double newValue, double oldValue) {
        refreshHealth(entity);
    }

    private void refreshHealth(CombatEntity entity) {
        if (entity == null || ignored.contains(entity.getUniqueId())) {
            return;
        }
        LivingEntity le = entity.getLivingEntity();
        if (le == null) {
            return;
        }
        double ceMaxHealth = entity.getStats().getStat(Stat.MAX_HEALTH);
        double ceHealth = entity.getStats().getRegeneratingStat(RegeneratingStat.HEALTH);
        double ratio = ceHealth / ceMaxHealth;
        double vanillaHealth = le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * ratio;

        if (vanillaHealth > 0) {
            le.setHealth(vanillaHealth);

        } else {
            if (!LoggingOut.isLoggingOut(le.getUniqueId())) {
                // setting health to 0 does not correctly thrown the death event or otherwise cause expected
                // behavior in bukkit. Must deal damage instead.
                knockbackStandIn.causeDamage(le, FATAL_DAMAGE);

            } else if (entity.isPlayer()) {
                // special case: death on player logout needs to be simulated, because causing damage in
                // order to do it will only cause bugs and not trigger death behaviors
                Player player = (Player) le;
                PlayerDeathEvent deathEvent =
                        new PlayerDeathEvent(player, new ArrayList<>(), 0, "");
                Bukkit.getPluginManager().callEvent(deathEvent);
            }
        }
    }

    /**
     * Deals vanilla damage to an entity.
     * @param target   The entity to deal damage to.
     * @param ceDamage The amount of combat engine damage to convert to vanilla damage.
     * @param origin   The origin point of the damage. Where the entity should be knocked back from upon
     *                 taking damage. <code>null</code> for no knockback.
     */
    private void damage(LivingEntity target, double ceDamage, Location origin) {
        if (ignored.contains(target.getUniqueId())) {
            return;
        }

        double vanillaDamage = StatUtils.convertHealth(target, ceDamage, true);
        if (vanillaDamage > 0) {

            AttackStandIn standIn;
            if (origin != null) {

                // XXX: To re-enable knockback on damage, set this to the knockbackStandIn
                knockbackStandIn.setCoordinates(origin.getX(), origin.getY(), origin.getZ());
                standIn = knockbackStandIn;

            } else {
                standIn = noKnockbackStandIn;
            }

            standIn.causeDamage(target, vanillaDamage);
        }
    }

    /**
     * Not 100% sure why dead entities are removed immediately. This delays their death for a
     * few moments so the animation can play out
     */
    private class DeathAnimationFixListener implements Listener {
		/*private List<UUID> ignore = new ArrayList<>();
		
		@EventHandler(priority = EventPriority.LOWEST)
		public void onNonPlayerDeath(CombatEngineDamageEvent event) {
			if(event.getDamaged().isPlayer()) return;
			
			if(ignore.contains(event.getDamaged().getLivingEntity().getUniqueId())) {
				event.setDamageMultiplier(0);
				return;
			}
		}
		
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onNonPlayerDeathStart(CombatEngineDamageEvent event) {
			if(event.getDamaged().isPlayer()) return;
			
			if(event.getDamaged().getStats().getRegeneratingStat(RegeneratingStat.HEALTH) - event.getFinalDamage() <= 0) {
				event.setDamageMultiplier(0);

				ignore.add(event.getDamaged().getUniqueId());
				
				event.getDamaged().getLivingEntity().playEffect(EntityEffect.DEATH);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						ignore.remove(event.getDamaged().getUniqueId());
						CombatEngine.inst().killEntity(event.getDamaged().getLivingEntity());
					}
				}.runTaskLater(CombatEngine.getPlugin(), CombatEngine.getPlugin().getConfig().getInt("death-destroy-timer", 15));
			}
		}*/
    }

    /**
     * Listens to and modifies bukkit events to replace vanilla behavior with CombatEngine features.
     */
    private class DamageListener implements Listener {

        /**
         * Cancels all damage not done by a preapproved source.
         * <p>
         * Some damage is not done from any source, but that damage does not throw any vanilla Bukkit
         * events.
         * @param event The damage event.
         */
        private void handleDamage(EntityDamageEvent event) {
            if (!(event instanceof EntityDamageByEntityEvent)) {
                event.setDamage(0.0);
                event.setCancelled(true);
                return;
            }

            EntityDamageByEntityEvent entEvent = (EntityDamageByEntityEvent) event;

            if (!isAttackerApproved(entEvent.getDamager())) {
                event.setDamage(0.0);
                event.setCancelled(true);
            } else {
                // event.setDamage(0);
            }
        }

        // assures listeners receive edited version of events
        @EventHandler(priority = EventPriority.LOWEST)
        public void onLowestEntityDamage(EntityDamageEvent event) {
            handleDamage(event);
        }

        // assures the final result is the edited version
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onEntityDamage(EntityDamageEvent event) {
            handleDamage(event);
        }

        // throws event to clarify when events are cancelled because they are not caused by CombatEngine
        @EventHandler(priority = EventPriority.MONITOR)
        public void onMonitorEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled()
                    && (!(event instanceof EntityDamageByEntityEvent) || !isAttackerApproved(((EntityDamageByEntityEvent) event)
                    .getDamager()))) {

                Bukkit.getServer().getPluginManager().callEvent(new VanillaDamageCancelledEvent(event));
            }
        }

        // gets whether an entity attack was caused by an approved entity
        private boolean isAttackerApproved(Entity attacker) {
            return knockbackStandIn.getEntity().equals(attacker)
                    || noKnockbackStandIn.getEntity().equals(attacker);
        }

        // stops normal health regeneration
        @EventHandler
        public void onEntityRegainHealth(EntityRegainHealthEvent event) {
            if (event.getRegainReason() != RegainReason.CUSTOM) {
                event.setAmount(0.0);
                event.setCancelled(true);
            }
        }

        // stops updating health until the player respawns
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerDeath(PlayerDeathEvent event) {
            CombatEntity ce = CombatEngine.getEntity(event.getEntity());
            if (ce != null && ce.isPlayer())
                ignored.add(event.getEntity().getUniqueId());
        }

        // sets MC health to its appropriate level after a respawn.
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerRespawn(PlayerRespawnEvent event) {
            ignored.remove(event.getPlayer().getUniqueId());
            refreshHealth(CombatEngine.getEntity(event.getPlayer()));
        }

        // ignores updates while stats are still being initialized.
        @EventHandler(priority = EventPriority.LOWEST)
        public void onCombatEntityPreCreate(CombatEntityPreCreateEvent event) {
            ignored.add(event.getLivingEntity().getUniqueId());
        }

        // ignores updates while stats are still being initialized. Also removes bad potion effects.
        @EventHandler(priority = EventPriority.MONITOR)
        public void onCombatEntityCreate(CombatEntityCreateEvent event) {
            LivingEntity le = event.getLivingEntity();
            for (PotionEffectType type : BAD_POTIONS) {
                if (le.hasPotionEffect(type)) {
                    le.removePotionEffect(type);

                    Logger lg = LegendsOfValeros.getInstance().getLogger();
                    lg.warning("A " + le.getType().name() + " had a " + type.getName() + " potion effect. "
                            + type.getName() + " is not compatible with CombatEngine and was removed! "
                            + "I recommend you stop this potion effect from being used in the first place, "
                            + "because it could cause major issues!");
                }
            }

            ignored.remove(event.getCombatEntity().getUniqueId());
            refreshHealth(event.getCombatEntity());
        }

        // makes sure to avoid memory leaks with ignored uuids
        @EventHandler
        public void onCombatEntityInvalidated(CombatEntityInvalidatedEvent event) {
            ignored.remove(event.getInvalidatedUuid());
        }
    }

}
