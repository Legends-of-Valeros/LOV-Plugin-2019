package com.legendsofvaleros.modules.combatengine.config;

import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.core.CombatProfile;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Default bukkit implementation of CombatEngine configurations.
 */
public class BukkitConfig implements CombatEngineConfig {
	private Map<Stat, String> statDescriptions;

	private CombatProfile defaultProfile;

	private double physicalDamageIncrease;
	private double magicDamageIncrease;
	private double armorSpellDamageReduction;
	private double armorPhysicalDamageReduction;
	private double resistanceSpellDamageReduction;

	private double normalSpeed;
	private double speedPointsPerPotionLevel;

	private double respawnHealthPercentage;
	private double respawnManaPercentage;
	private double respawnEnergyPercentage;

	private double unattributedHitChance;

	private double critMultiplier;
	private double unattributedCritChance;

	private long regenIntervalTicks;
	private Map<RegeneratingStat, Double> regenPercentagesPerPoint;

	private long historyExpirationMillis;
	private double maxKillDistance;

	private double maxTargetingDistancesSquared;
	// hard coded for now, until there is a reasonable need to configure it
	private long validationCheckTicks = 20;
	private double threatReductionPerCheck;

	public BukkitConfig() {
		load();
	}

	public void reload() {
		CombatEngine.getInstance().reloadConfig();
		load();
	}

	private void load() {
		FileConfiguration config = CombatEngine.getInstance().getConfig();

		ConfigurationSection descSec = config.getConfigurationSection("stat-description");
		statDescriptions = new HashMap<>();
		for(String key : descSec.getKeys(false)) {
			try {
				statDescriptions.put(Stat.valueOf(key), descSec.getString(key));
			} catch (Exception ex) {
				CombatEngine.getInstance().getLogger().severe("Could not load stat description. '" + key + "' is not a recognized stat");
				MessageUtil.sendException(CombatEngine.getInstance(), ex);
			}
		}

		// default profile
		ConfigurationSection profileSec = config.getConfigurationSection("default-combat-profile");
		defaultProfile = new CombatProfile();
		for (String key : profileSec.getKeys(false)) {
			double value = profileSec.getDouble(key);
			try {
				Stat stat = Stat.valueOf(key);
				defaultProfile.setBaseStat(stat, value);

			} catch (Exception ex) {
				try {
					RegeneratingStat rStat = RegeneratingStat.valueOf(key);
					defaultProfile.setBaseRegeneratingStat(rStat, value);

				} catch (Exception ex2) {
					CombatEngine.getInstance().getLogger().severe(
							"Could not load the default profile. '" + key + "' is not a recognized stat");
					MessageUtil.sendException(CombatEngine.getInstance(), ex);
					MessageUtil.sendException(CombatEngine.getInstance(), ex2);
				}
			}
		}

		// armor, attack damage, magic damage, and resistances
		ConfigurationSection damageSec = config.getConfigurationSection("damage-modifiers");
		ConfigurationSection offenseSec = damageSec.getConfigurationSection("offensive");
		physicalDamageIncrease = offenseSec.getDouble("physical-attack-per-point");
		magicDamageIncrease = offenseSec.getDouble("magic-attack-per-point");
		ConfigurationSection defenseSec = damageSec.getConfigurationSection("defensive");
		ConfigurationSection armorSec = defenseSec.getConfigurationSection("armor");
		armorPhysicalDamageReduction = armorSec.getDouble("physical-reduction-per-point");
		armorSpellDamageReduction = armorSec.getDouble("magic-reduction-per-point");
		resistanceSpellDamageReduction = defenseSec.getDouble("resistances.damage-reduction-per-point");

		// speed options
		ConfigurationSection speedSec = config.getConfigurationSection("speed-stat");
		normalSpeed = speedSec.getDouble("stat-for-normal-speed");
		speedPointsPerPotionLevel = speedSec.getDouble("points-per-potion-level");

		// player respawns
		ConfigurationSection respawnSec = config.getConfigurationSection("player-respawns");
		respawnHealthPercentage = respawnSec.getDouble("health-percentage");
		respawnManaPercentage = respawnSec.getDouble("mana-percentage");
		respawnEnergyPercentage = respawnSec.getDouble("energy-percentage");

		// hit chances
		unattributedHitChance = config.getDouble("dodging.environmental-hit-chance");

		// critical hits
		ConfigurationSection critSec = config.getConfigurationSection("critical-hits");
		critMultiplier = critSec.getDouble("damage-multiplier");
		unattributedCritChance = critSec.getDouble("environmental-crit-chance");

		// regen
		ConfigurationSection regenSec = config.getConfigurationSection("regeneration");
		regenIntervalTicks = regenSec.getLong("regen-period-seconds") * 20;
		regenPercentagesPerPoint = new HashMap<>();
		ConfigurationSection regenPercentSec =
				regenSec.getConfigurationSection("percent-of-max-per-regen-stat");
		for (String key : regenPercentSec.getKeys(false)) {
			try {
				RegeneratingStat stat = RegeneratingStat.valueOf(key);
				double value = regenPercentSec.getDouble(key);
				regenPercentagesPerPoint.put(stat, value);
			} catch (Exception e) {
				CombatEngine.getInstance().getLogger().severe(
						"Could not load regeneration percentages, '" + key
						+ "' is not a recognized regenerating stat!");
			}
		}

		// damage attribution
		ConfigurationSection attSec = config.getConfigurationSection("damage-attribution");
		maxKillDistance = attSec.getDouble("max-kill-distance");
		historyExpirationMillis = attSec.getLong("history-expiration-seconds") * 1000;

		// threat and AI targeting
		ConfigurationSection threatSec = config.getConfigurationSection("threat");
		double unSquared = threatSec.getDouble("max-targeting-distance");
		maxTargetingDistancesSquared = unSquared * unSquared;
		threatReductionPerCheck =
				(validationCheckTicks / 20.0) / (double) threatSec.getInt("threat-expiration-seconds");
	}

	@Override
	public String getStatDescription(Stat stat) {
		return statDescriptions.get(stat);
	}

	@Override
	public CombatProfile getDefaultProfile() {
		return defaultProfile;
	}

	@Override
	public double getPhysicalDamageIncrease() {
		return physicalDamageIncrease;
	}

	@Override
	public double getMagicDamageIncrease() {
		return magicDamageIncrease;
	}

	@Override
	public double getArmorSpellDamageReduction() {
		return armorSpellDamageReduction;
	}

	@Override
	public double getArmorPhysicalDamageReduction() {
		return armorPhysicalDamageReduction;
	}

	@Override
	public double getResistanceSpellDamageReduction() {
		return resistanceSpellDamageReduction;
	}

	@Override
	public double getNormalSpeed() {
		return normalSpeed;
	}

	@Override
	public double getSpeedPointsPerPotionLevel() {
		return speedPointsPerPotionLevel;
	}

	@Override
	public double getRespawnHealthPercentage() {
		return respawnHealthPercentage;
	}

	@Override
	public double getRespawnManaPercentage() {
		return respawnManaPercentage;
	}

	@Override
	public double getRespawnEnergyPercentage() {
		return respawnEnergyPercentage;
	}

	@Override
	public double getUnattributedHitChance() {
		return unattributedHitChance;
	}

	@Override
	public double getCritMultiplier() {
		return critMultiplier;
	}

	@Override
	public double getUnattributedCritChance() {
		return unattributedCritChance;
	}

	@Override
	public long getRegenIntervalTicks() {
		return regenIntervalTicks;
	}

	@Override
	public double getRegenPercentagePerPoint(RegeneratingStat stat) {
		return regenPercentagesPerPoint.get(stat);
	}

	@Override
	public long getHistoryExpirationMillis() {
		return historyExpirationMillis;
	}

	@Override
	public double getMaxKillDistance() {
		return maxKillDistance;
	}

	@Override
	public double getMaxTargetingDistanceSquared() {
		return maxTargetingDistancesSquared;
	}

	@Override
	public long getValidationCheckTicks() {
		return validationCheckTicks;
	}

	@Override
	public double getThreatReductionPerCheck() {
		return threatReductionPerCheck;
	}

}
