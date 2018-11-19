package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.config.StatRegenerationConfig;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineRegenEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityInvalidatedEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Restores regenerable stats over time.
 * <p>
 * Regenerates stats on the basis of a variable percentage of their potential max. If an entity has
 * a higher regen stat, each interval they will regen a larger percentage of their max.
 * <p>
 * Splits entities on the server into a number of subgroups. This allows for regeneration to happen
 * in waves rather than having to iterate over every entity on the server at once, blocking the main
 * thread for a potentially problematic amount of time.
 * <p>
 * Balances groups by inserting into the least full group, but does not ensure that the different
 * groups stay roughly the same length over time except through this priority-based insertion.
 */
public class StatRegenerator {

	private static final int NUM_GROUPS = 10;

	private static final List<RegeneratingStat> REGEN = new ArrayList<>(
            Arrays.asList(RegeneratingStat.values()));

	private StatRegenerationConfig config;

	private Map<UUID, Integer> mapIndexes;
	private List<Map<UUID, CombatEntity>> entities;

	public StatRegenerator(StatRegenerationConfig config) {
		this.config = config;

		mapIndexes = new HashMap<>();
		entities = new ArrayList<>(NUM_GROUPS);
		for (int i = 0; i < NUM_GROUPS; i++) {
			entities.add(i, new HashMap<>());
		}

		CombatEngine.getInstance().registerEvents(new EntityListener());
		new RegenTask();
	}

	/**
	 * Regenerates entities' stats per group.
	 */
	private class RegenTask extends BukkitRunnable {

		private int regenCounter;

		private RegenTask() {
			if (config.getRegenIntervalTicks() % NUM_GROUPS != 0) {
				throw new IllegalArgumentException("The regen interval cannot be evenly divided into "
						+ NUM_GROUPS + " groups!");
			}
			long interval = config.getRegenIntervalTicks() / NUM_GROUPS;
			runTaskTimer(LegendsOfValeros.getInstance(), interval, interval);
		}

		@Override
		public void run() {
			if (++regenCounter >= NUM_GROUPS) {
				regenCounter = 0;
			}

			for (CombatEntity ce : entities.get(regenCounter).values()) {
				LivingEntity le = ce.getLivingEntity();
				if (le != null && !le.isDead() && le.isValid()) {
					for (RegeneratingStat restore : REGEN) {
						double max = ce.getStats().getStat(restore.getMaxStat());
						double regenStat = ce.getStats().getStat(restore.getRegenStat());

						double regenPercentage = (regenStat * config.getRegenPercentagePerPoint(restore));
						double regenAmount = max * regenPercentage;

						CombatEngineRegenEvent event = new CombatEngineRegenEvent(ce, restore, regenAmount);
						Bukkit.getServer().getPluginManager().callEvent(event);

						if (!event.isCancelled() && event.getRegenerationAmount() > 0) {
							ce.getStats().editRegeneratingStat(restore, event.getRegenerationAmount());
						}
					}
				}
			}
		}
	}

	/**
	 * Populates/depopulates the entity maps.
	 */
	private class EntityListener implements Listener {

		@EventHandler(priority = EventPriority.MONITOR)
		public void onCombatEntityCreate(CombatEntityCreateEvent event) {
			UUID uid = event.getLivingEntity().getUniqueId();

			// inserts into the least full group to do some basic balancing between them
			int insertIndex = 0;
			int minSize = Integer.MAX_VALUE;
			for (int i = 0; i < entities.size(); i++) {
				int size = entities.get(i).size();
				if (size < minSize) {
					insertIndex = i;
					minSize = size;
				}
			}

			mapIndexes.put(uid, insertIndex);
			entities.get(insertIndex).put(uid, event.getCombatEntity());
		}

		@EventHandler(priority = EventPriority.MONITOR)
		public void onCombatEntityInvalidated(CombatEntityInvalidatedEvent event) {
			UUID uid = event.getInvalidatedUuid();
			Integer mapIndex = mapIndexes.remove(uid);
			if (mapIndex != null) {
				entities.get(mapIndex).remove(uid);
			}
		}
	}

}
