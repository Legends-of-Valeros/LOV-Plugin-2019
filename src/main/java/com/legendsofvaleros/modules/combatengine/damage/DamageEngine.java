package com.legendsofvaleros.modules.combatengine.damage;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.config.CombatEngineConfig;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.core.MinecraftHealthHandler;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.events.*;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.DebugFlags;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import com.legendsofvaleros.util.Utilities;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Deals CombatEngine damage.
 */
public class DamageEngine {

	private HitAndCritCalculator hitAndCit;
	private DamageAttributer attributer;
	public DamageAttributer getAttributer() { return attributer; }
	
	private MinecraftHealthHandler mcHealth;
	private DamageMultiplier multiplierHandler;

	public DamageEngine(CombatEngineConfig config, MinecraftHealthHandler mcHealth) {
		this.mcHealth = mcHealth;
		this.hitAndCit = new HitAndCritCalculator(config, config);
		this.attributer = new DamageAttributer(config);
		this.multiplierHandler = new DamageMultiplier(config, config);

		CombatEngine.getInstance().registerEvents(new DeathListener());
		CombatEngine.getInstance().registerEvents(new Debugging());
	}

	public boolean causeSpellDamage(LivingEntity target, LivingEntity attacker, SpellType type,
			double baseDamage, Location damageOrigin, boolean canMiss, boolean canCrit) {
		if (target == null || baseDamage <= 0) {
			return false;
		}
		if (type == null) {
			type = SpellType.OTHER;
		}

		CombatEntity ceTarget = CombatEngine.getEntity(target);
		if(ceTarget == null)
			return false;

		CombatEntity ceAttacker = CombatEngine.getEntity(attacker);

		// does the attack hit?
		if (canMiss && !hitAndCit.doesAttackHit(ceTarget, ceAttacker)) {
			CombatEngineAttackMissEvent event = new CombatEngineAttackMissEvent(ceTarget, ceAttacker);
			Bukkit.getServer().getPluginManager().callEvent(event);
			return false;
		}

		// does the attack crit?
		boolean crit = canCrit && hitAndCit.doesAttackCrit(ceTarget, ceAttacker);

		CombatEngineSpellDamageEvent event =
				new CombatEngineSpellDamageEvent(ceTarget, ceAttacker, damageOrigin, baseDamage, crit, type);

		event.newDamageModifierBuilder("Resistance")
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue(multiplierHandler.getSpellDamageMultiplier(ceTarget, ceAttacker, crit, type))
			.build();
		
		return handleEvent(event);
	}

	public boolean causePhysicalDamage(LivingEntity target, LivingEntity attacker, PhysicalType type,
			double baseDamage, Location damageOrigin, boolean canMiss, boolean canCrit) {
		if (target == null || baseDamage <= 0) {
			return false;
		}
		if (type == null) {
			type = PhysicalType.OTHER;
		}
		
		CombatEntity ceTarget = CombatEngine.getEntity(target);
		if(ceTarget == null)
			return false;

		CombatEntity ceAttacker = CombatEngine.getEntity(attacker);
		
		// does the attack hit?
		if (canMiss && !hitAndCit.doesAttackHit(ceTarget, ceAttacker)) {
			CombatEngineAttackMissEvent event = new CombatEngineAttackMissEvent(ceTarget, ceAttacker);
			Bukkit.getServer().getPluginManager().callEvent(event);
			return false;
		}

		// does the attack crit?
		boolean crit = canCrit && hitAndCit.doesAttackCrit(ceTarget, ceAttacker);

		CombatEnginePhysicalDamageEvent event =
				new CombatEnginePhysicalDamageEvent(ceTarget, ceAttacker, damageOrigin, baseDamage, crit, type);

		event.newDamageModifierBuilder("Resistance")
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue(multiplierHandler.getPhysicalDamageMultiplier(ceTarget, ceAttacker, crit, type))
			.build();

		return handleEvent(event);
	}

	public boolean causeTrueDamage(LivingEntity target, LivingEntity attacker, double damage,
			Location damageOrigin) {
		if (target == null || damage <= 0) {
			return false;
		}

		CombatEntity ceTarget = CombatEngine.getEntity(target);
		if(ceTarget == null)
			return false;

		CombatEntity ceAttacker = CombatEngine.getEntity(attacker);

		CombatEngineTrueDamageEvent event =
				new CombatEngineTrueDamageEvent(ceTarget, ceAttacker, damageOrigin, damage);
		
		return handleEvent(event);
	}

	public void killEntity(LivingEntity target) {
		if(target == null) return;

		CombatEntity ceTarget = CombatEngine.getEntity(target);
		if(ceTarget == null) return;

		CombatEngineDeathEvent vEvent = new CombatEngineDeathEvent(ceTarget, null);
		Bukkit.getServer().getPluginManager().callEvent(vEvent);
		
		target.remove();
	}

	private boolean handleEvent(CombatEngineDamageEvent event) {
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled() && event.getFinalDamage() > 0) {
			CombatEntity ce = event.getDamaged();

			// tracks the damage so it can be attributed
			attributer.reportDamage(event.getDamaged().getLivingEntity(),
					(event.getAttacker() != null ? event.getAttacker().getLivingEntity() : null),
					event.getFinalDamage());

			// passes on information about knockbacks
			mcHealth.setNextDamageOrigin(event.getDamageOrigin());

			// applies the reduction in health
			ce.getStats().editRegeneratingStat(RegeneratingStat.HEALTH, -1 * event.getFinalDamage());
			
			return true;

		} else {
			return false;
		}
	}

	/**
	 * Informs listeners of kill attribution.
	 */
	public class DeathListener implements Listener {
		public DeathListener() { }

		@EventHandler(priority = EventPriority.LOWEST)
		public void onEntityDeath(EntityDeathEvent event) {
			LivingEntity killer = attributer.onDeath(event.getEntity());
			
			// does not attribute suicides in the called event
			if (event.getEntity().equals(killer)) {
				killer = null;
			}

			CombatEntity ceDied = CombatEngine.getEntity(event.getEntity());
			CombatEntity ceKiller = CombatEngine.getEntity(killer);

			if (ceDied != null) {
				if(!ceDied.isPlayer()) {
					event.getEntity().getEquipment().clear();
					event.getDrops().clear();
					
					if(ceDied.getLivingEntity() instanceof Player) {
						((Player)ceDied.getLivingEntity()).getInventory().clear();
					}
				}
				
				CombatEngineDeathEvent vEvent = new CombatEngineDeathEvent(ceDied, ceKiller);
				Bukkit.getServer().getPluginManager().callEvent(vEvent);
				event.getDrops().addAll(vEvent.drops);
			}
		}
	}

	public class Debugging implements Listener {
		private final DecimalFormat DF = new DecimalFormat("#.00");

		public Debugging() { }

		@EventHandler(priority = EventPriority.LOWEST)
		public void onAttack(CombatEngineAttackMissEvent event) {
			if(!event.getAttacker().isPlayer()) return;
			
			Player p = (Player)event.getAttacker().getLivingEntity();
			if(DebugFlags.is(p) && DebugFlags.get(p).damage)
				MessageUtil.sendInfo(p, "[MISS] 0.00 = 0.00 * 0.00 * 0.00 * 0.00");
		}
		
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onTrue(CombatEngineTrueDamageEvent event) {
			output("TRUE", event);
		}
		
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onSpell(CombatEngineSpellDamageEvent event) {
			output("SPEL", event);
		}
		
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onPhysical(CombatEnginePhysicalDamageEvent event) {
			output("PHYS", event);
		}

		public void output(String prefix, CombatEngineDamageEvent event) {
			if(event.getAttacker() == null || event.getDamaged() == null) return;
			if(!event.getAttacker().isPlayer() && !event.getDamaged().isPlayer()) return;

			Player pA = event.getAttacker().isPlayer() ? (Player)event.getAttacker().getLivingEntity() : null;
			boolean pAOP = pA != null && (DebugFlags.is(pA) && DebugFlags.get(pA).damage);
			Player pD = event.getDamaged().isPlayer() ? (Player)event.getDamaged().getLivingEntity() : null;
			boolean pDOP = pD != null && (DebugFlags.is(pD) && DebugFlags.get(pD).damage);

			if(!pAOP && !pDOP)
				return;

			if(event.isCriticalHit()) prefix += "*";
			else prefix += " ";
			
			prefix += ": ";
			
			if(event.isCancelled()) {
				if(pAOP)
					MessageUtil.sendInfo(pA, prefix + "Damage prevented by a plugin.");
				if(pDOP)
					MessageUtil.sendInfo(pD, prefix + "Damage prevented by a plugin.");
			}else{
				TextBuilder tb = new TextBuilder(prefix)
						.append(DF.format(event.getFinalDamage())).color(ChatColor.AQUA).hover("Final Damage")
						.append(" = ");

				tb.append("((");

				tb.append(DF.format(event.getBaseDamage())).hover("Base Damage");

				for(Map.Entry<String, ValueModifier> entry : event.getModifiers().entrySet()) {
					if(entry.getValue().getType() != ValueModifierBuilder.ModifierType.FLAT_EDIT) continue;
					tb.append(" + ").append(DF.format(entry.getValue().getValue())).color(ChatColor.YELLOW).hover(entry.getKey());
				}

				tb.append(")");

				for(Map.Entry<String, ValueModifier> entry : event.getModifiers().entrySet()) {
					if(entry.getValue().getType() != ValueModifierBuilder.ModifierType.MULTIPLIER) continue;
					tb.append(" * ").append(DF.format(entry.getValue().getValue())).color(ChatColor.YELLOW).hover(entry.getKey());
				}

				tb.append(")");

				for(Map.Entry<String, ValueModifier> entry : event.getModifiers().entrySet()) {
					if(entry.getValue().getType() != ValueModifierBuilder.ModifierType.FLAT_EDIT_IGNORES_MULTIPLIERS) continue;
					tb.append(" + ").append(DF.format(entry.getValue().getValue())).color(ChatColor.YELLOW).hover(entry.getKey());
				}

				BaseComponent[] bc = tb.create();

				if(pAOP) {
					MessageUtil.sendInfo(pA, MessageUtil.prepend(bc,
							new TextBuilder(">").create()));
				}
				if(pDOP) {
					MessageUtil.sendInfo(pD, MessageUtil.prepend(bc,
							new TextBuilder("<").create()));
				}
			}
		}
	}
}
