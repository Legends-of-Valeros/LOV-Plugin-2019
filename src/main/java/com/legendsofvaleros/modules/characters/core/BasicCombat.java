package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.config.CharactersConfig;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.events.CombatEnginePhysicalDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.VanillaDamageCancelledEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.StatUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implements CombatEngine-compatible combat for basic things like melee attacks and normal
 * projectiles.
 */
public class BasicCombat {

    // TODO should bows be included in this, or should they be handled specially for rogues? Are other
    // classes going to be able to use bows?

    private final CharactersConfig config;

    public BasicCombat(CharactersConfig config) {
        this.config = config;
        Characters.getInstance().registerEvents(new CombatListener());
        Characters.getInstance().registerEvents(new DamageListener());
    }

    /**
     * Listens to basic player combat and translates into CombatEngine-compatible combat.
     */
    private class CombatListener implements Listener {
        public final Map<UUID, Long> lastSwing = new HashMap<>();

        @EventHandler
        public void onPlayerLogout(PlayerQuitEvent event) {
            lastSwing.remove(event.getPlayer().getUniqueId());
        }

        // translates melee attacks by players into CombatEngine damage in line with their class and its
        // melee stats
        @EventHandler
        public void onVanillaDamageCancelled(VanillaDamageCancelledEvent vEvent) {
            if (vEvent.isCancelled()) return;

            EntityDamageEvent dEvent = vEvent.getCancelledEvent();
            if (!(dEvent instanceof EntityDamageByEntityEvent)) {
                return;
            }
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) dEvent;

            // Cancel out all vanilla damage.
            event.setDamage(0);

            if (!(event.getDamager() instanceof LivingEntity)
                    || !(event.getEntity() instanceof LivingEntity)
                    || event.getDamager().getType() != EntityType.PLAYER) {
                return;
            }

            Player player = (Player) event.getDamager();
            PlayerCharacter current = (Characters.isPlayerCharacterLoaded(player) ? Characters.getPlayerCharacter(player) : null);
            if (current == null) {
                return;
            }

            double baseDamage = config.getClassConfig(current.getPlayerClass()).getBaseMeleeDamage();

            // causes damage through CombatEngine to replace the invalidated vanilla damage
            CombatEngine.getInstance().causePhysicalDamage((LivingEntity) event.getEntity(),
                    (LivingEntity) event.getDamager(), PhysicalType.MELEE, baseDamage,
                    event.getDamager().getLocation(), true, true);
        }

        @EventHandler
        public void onDamageEvent(CombatEnginePhysicalDamageEvent event) {
            if (!event.getAttacker().isPlayer()) return;

            Player player = (Player) event.getAttacker().getLivingEntity();

            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);

            long millis = System.currentTimeMillis();
            Long last = lastSwing.get(player.getUniqueId());
            if (last == null) last = 0L;

            // The amount of time that must be awaited before full damage is dealt.
            long wait = (long) (1D / attr.getValue() * 1000);

            // The amount of time remaining to be at full power.
            long remaining = last + wait - millis;

            if (remaining > 0) {
                event.newDamageModifierBuilder("Swing Multiplier")
                        .setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
                        .setValue(1D - ((double) remaining / wait))
                        .build();
                //swingMultiplier = Math.pow(x, x + 3); // Creates a power curve between 0 and 1.
            }

            lastSwing.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Listens to general damage and translates into CombatEngine-compatible damage.
     */
    private class DamageListener implements Listener {

        // translates general damage into CombatEngine damage in line with their class and its stats
        @EventHandler
        public void onVanillaDamageCancelled(VanillaDamageCancelledEvent vEvent) {
            EntityDamageEvent event = vEvent.getCancelledEvent();
            if (!(event.getEntity() instanceof LivingEntity))
                return;

            LivingEntity attacked = (LivingEntity) event.getEntity();
            LivingEntity attacker = (vEvent.isDamageByEntity() ? ((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity ? (LivingEntity) ((EntityDamageByEntityEvent) event).getDamager() : null : null);

            double baseDamage = event.getDamage();

            byte damageType = 0;
            SpellType spellType = null;
            PhysicalType physicalType = PhysicalType.MELEE;

            switch (event.getCause()) {
                default:
                    damageType = 1;
                    baseDamage = StatUtils.convertHealth((LivingEntity) event.getEntity(), baseDamage, false);
                    break;
                case FALL:
                    physicalType = PhysicalType.OTHER;
                    baseDamage /= 1.3;
                    break;
                case PROJECTILE:
                    physicalType = PhysicalType.MISC_PROJECTILE;
                    break;
                case MAGIC:
                    damageType = 2;
                    spellType = SpellType.OTHER;
                    break;
                case POISON:
                    damageType = 2;
                    spellType = SpellType.POISON;
                    break;
            }

            // causes damage through CombatEngine to replace the invalidated vanilla damage
            if (damageType == 1)
                CombatEngine.getInstance().causeTrueDamage(attacked, attacker, baseDamage, null);
            else if (damageType == 2)
                CombatEngine.getInstance().causeSpellDamage(attacked, attacker, spellType, baseDamage,
                        attacked.getLocation(), true, true);
            else
                CombatEngine.getInstance().causePhysicalDamage(attacked, attacker, physicalType, baseDamage,
                        attacked.getLocation(), true, true);
        }
    }

}
