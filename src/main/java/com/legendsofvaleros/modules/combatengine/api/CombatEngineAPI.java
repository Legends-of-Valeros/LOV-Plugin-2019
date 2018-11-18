package com.legendsofvaleros.modules.combatengine.api;

import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Top-level CombatEngine interface.
 * <p>
 * CombatEngine obscures Bukkit's normal combat system and creates a new, entirely custom one based
 * on MMO-style gaming conventions.
 */
public interface CombatEngineAPI {

	/**
	 * Gets combat-relevant information for an entity.
	 * <p>
	 * The object returned may not be valid over long periods of time, such as between logins for a
	 * player. Do not reference locally over long periods. Instead, re-obtain the object through this
	 * method.
	 * 
	 * @param getFor The entity to get combat information for.
	 * @return The combat-relevant information for the given entity.
	 */
	CombatEntity getCombatEntity(LivingEntity getFor);

	/**
	 * Inflicts spell damage on an entity.
	 * 
	 * @param target The entity to inflict damage on.
	 * @param attacker The entity causing the damage, if any. Can be <code>null</code> if the cause is
	 *        or should be ambiguous.
	 * @param type The type of spell damage being inflicted.
	 * @param baseDamage The base amount of damage to deal. Will be affected by offensive and
	 *        defensive stats and resistances. Should not take into account the stats tracked by
	 *        CombatEngine. For example, if a level 10 ice spell's basic damage is <code>525.0</code>
	 *        before any stats boosts or special items, use <code>525.0</code> and CombatEngine will
	 *        modify the value based on spell damage, defense, and ice resistance stats.
	 * @param damageOrigin The location from which the damage originated. This will affect where the
	 *        damaged entity gets "knocked back" from. Can be <code>null</code> to avoid all
	 *        knockback.
	 * @param canMiss <code>true</code> if it should be possible for this damage to miss, depending on
	 *        a random chance and the stats of the attacker/defender. <code>false</code> if it should
	 *        not be able to miss.
	 * @param canCrit <code>true</code> if it should be possible for this damage to be a critical hit
	 *        and do extra damage, depending on the stats of the attacker/defender. <code>false</code>
	 *        if it should not be able to crit.
	 * @return <code>true</code> if any damage is successfully done. <code>false</code> if the damage
	 *         misses, is cancelled, or is reduced to <code>0</code> or less.
	 */
	boolean causeSpellDamage(LivingEntity target, LivingEntity attacker, SpellType type,
                             double baseDamage, Location damageOrigin, boolean canMiss, boolean canCrit);

	/**
	 * Inflicts physical damage on an entity.
	 * 
	 * @param target The entity to inflict damage on.
	 * @param attacker The entity causing the damage, if any. Can be <code>null</code> if the cause is
	 *        or should be ambiguous.
	 * @param type The type of physical damage being inflicted.
	 * @param baseDamage The base amount of damage to deal. Will be affected by offensive and
	 *        defensive stats and resistances. Should not take into account the stats tracked by
	 *        CombatEngine. For example, if a level 10 arrow hit's basic damage is <code>265.0</code>,
	 *        before any stats boosts or special items, use <code>265.0</code> and CombatEngine will
	 *        modify the value based on physical attack, defense, and ranged resistance stats.
	 * @param damageOrigin The location from which the damage originated. This will affect where the
	 *        damaged entity gets "knocked back" from. Can be <code>null</code> to avoid all
	 *        knockback.
	 * @param canMiss <code>true</code> if it should be possible for this damage to miss, depending on
	 *        a random chance and the stats of the attacker/defender. <code>false</code> if it should
	 *        not be able to miss.
	 * @param canCrit <code>true</code> if it should be possible for this damage to be a critical hit
	 *        and do extra damage, depending on the stats of the attacker/defender. <code>false</code>
	 *        if it should not be able to crit.
	 * @return <code>true</code> if any damage is successfully done. <code>false</code> if the damage
	 *         misses, is cancelled, or is reduced to <code>0</code> or less.
	 */
	boolean causePhysicalDamage(LivingEntity target, LivingEntity attacker, PhysicalType type,
                                double baseDamage, Location damageOrigin, boolean canMiss, boolean canCrit);

	boolean causePhysicalDamage(LivingEntity target, LivingEntity attacker, PhysicalType type,
                                double baseDamage, double swingMultiplier, Location damageOrigin, boolean canMiss, boolean canCrit);

	/**
	 * Inflicts true damage on an entity, not affected by defensive stats or resistances.
	 * <p>
	 * True damage ignores defensive stats, cannot be dodged, and cannot be a critical hit.
	 * 
	 * @param target The entity to inflict damage on.
	 * @param attacker The entity causing the damage, if any. Can be <code>null</code> if the cause is
	 *        or should be ambiguous.
	 * @param damage The amount of damage to deal. Will <b>not</b> be affected by offensive or
	 *        defensive stats or resistances.
	 * @param damageOrigin The location from which the damage originated. This will affect where the
	 *        damaged entity gets "knocked back" from. Can be <code>null</code> to avoid all
	 *        knockback.
	 * @return <code>true</code> if any damage is successfully done. <code>false</code> if the damage
	 *         misses, is cancelled, or is reduced to <code>0</code> or less.
	 */
	boolean causeTrueDamage(LivingEntity target, LivingEntity attacker, double damage,
                            Location damageOrigin);

	/**
	 * Forces the entity to removed in much the way that entity.remove() works (It actually
	 * call this in the background). Does not fire damage events, but fires
	 * CombatEngineDeathEvent so plugins may do their cleanup.
	 */
	void killEntity(LivingEntity target);
}
