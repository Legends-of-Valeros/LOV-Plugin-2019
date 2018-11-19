package com.legendsofvaleros.modules.combatengine.damage;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.config.CombatEngineConfig;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.core.MinecraftHealthHandler;
import com.legendsofvaleros.modules.combatengine.damage.physical.PhysicalType;
import com.legendsofvaleros.modules.combatengine.damage.spell.SpellType;
import com.legendsofvaleros.modules.combatengine.events.*;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.DebugFlags;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import mkremins.fanciful.FancyMessage;
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

		double resistanceMultiplier = multiplierHandler.getSpellDamageMultiplier(ceTarget, ceAttacker, crit, type);

		CombatEngineSpellDamageEvent event =
				new CombatEngineSpellDamageEvent(ceTarget, ceAttacker, damageOrigin, baseDamage,
						resistanceMultiplier, crit, type);
		
		return handleEvent(event);
	}

	public boolean causePhysicalDamage(LivingEntity target, LivingEntity attacker, PhysicalType type,
			double baseDamage, double swingMultiplier, Location damageOrigin, boolean canMiss, boolean canCrit) {
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

		double resistanceMultiplier =
				multiplierHandler.getPhysicalDamageMultiplier(ceTarget, ceAttacker, crit, type);

		CombatEnginePhysicalDamageEvent event =
				new CombatEnginePhysicalDamageEvent(ceTarget, ceAttacker, damageOrigin, baseDamage,
                        resistanceMultiplier, swingMultiplier, crit, type);
		
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
		if(event.getFinalDamage() <= 0)
			return false;
		
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
			boolean pDOP = pD != null && (Utilities.isOp(pD) && DebugFlags.is(pD) && DebugFlags.get(pD).damage);
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
				FancyMessage fm = new FancyMessage(prefix)
						.then(DF.format(event.getFinalDamage())).color(ChatColor.AQUA).tooltip("Final Damage")
						.then(" = ")
						.then(DF.format(event.getRawDamage())).tooltip("Raw Damage")
						.then(" * ")
						.then(DF.format(event.getSwingMultiplier())).tooltip("Swing Multiplier")
						.then(" * ")
						.then(DF.format(event.getDamageMultiplier())).tooltip("Damage Multiplier");
				if(pAOP)
					MessageUtil.sendInfo(pA, MessageUtil.prepend(fm, new FancyMessage(event.getAttacker().getLivingEntity().getName())
							.then("(")
							.then(String.valueOf(event.getAttacker().getStats().getRegeneratingStat(RegeneratingStat.HEALTH))).color(ChatColor.GRAY)
							.then("/")
							.then(String.valueOf(event.getAttacker().getStats().getStat(Stat.MAX_HEALTH))).color(ChatColor.GRAY)
							.then(" > ")));
				if(pDOP)
					MessageUtil.sendInfo(pD, MessageUtil.prepend(fm, new FancyMessage("<")));
			}
		}
	}
}
