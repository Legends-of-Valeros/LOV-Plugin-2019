package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default bukkit-config implementation of the Characters configuration.
 */
public class BukkitConfig implements CharactersConfig {
	private String dbpoolsId;

	private Map<AbilityStat, String> statDescriptions;

	private Location createLoc;
	private Location startLoc;
	
	private String creationStartNPC;
	private List<String> creationStartText, creationCreateText;

	private long millisUntilOutOfPvp;
	private long millisUntilOutOfPve;

	private int maxLevel;
	private Map<Integer, Long> xpToLevel;

	private Map<EntityClass, ClassConfig> classConfigs;
	private Map<EntityRace, RaceConfig> raceConfigs;

	public BukkitConfig() {
		load();
	}

	private void load() {
		FileConfiguration config = Characters.getInstance().getConfig();

		dbpoolsId = LegendsOfValeros.getInstance().getConfig().getString("dbpools-database");

		ConfigurationSection descSec = config.getConfigurationSection("ability-stat-description");
		statDescriptions = new HashMap<>();
		for(String key : descSec.getKeys(false)) {
			try {
				statDescriptions.put(AbilityStat.valueOf(key), descSec.getString(key));
			} catch (Exception ex) {
				Characters.getInstance().getLogger().severe("Could not load ability stat description. '" + key + "' is not a recognized stat");
				MessageUtil.sendException(Characters.getInstance(), ex);
			}
		}

		ConfigurationSection locSec = config.getConfigurationSection("locations");
		createLoc = parseLocation(locSec.getString("create-location"));
		startLoc = parseLocation(locSec.getString("start-location"));

		creationStartText = config.getStringList("creation-start-text");
		creationStartNPC = config.getString("creation-create-npc");
		creationCreateText = config.getStringList("creation-create-text");

		ConfigurationSection comLogSec = config.getConfigurationSection("combat-status");
		millisUntilOutOfPve = comLogSec.getLong("seconds-until-out-of-pve") * 1000;
		millisUntilOutOfPvp = comLogSec.getLong("seconds-until-out-of-pvp") * 1000;

		ConfigurationSection levelSec = config.getConfigurationSection("leveling");
		maxLevel = levelSec.getInt("max-level");
		xpToLevel = new HashMap<>();
		ConfigurationSection xpSec = levelSec.getConfigurationSection("xp-to-level");
		for (String key : xpSec.getKeys(false)) {
			int level = Integer.valueOf(key);
			long xp = xpSec.getLong(key);

			xpToLevel.put(level, xp);
		}

		classConfigs = new HashMap<>();
		for (EntityClass playerClass : EntityClass.values()) {
			try {
				classConfigs.put(playerClass, new BukkitClassConfig(playerClass));
			} catch (Exception e) {
				Characters.getInstance().getLogger().severe("Encountered an issue while loading the configuration for the class '" + playerClass.name() + "'");
				MessageUtil.sendException(Characters.getInstance(), e);
			}
		}

		raceConfigs = new HashMap<>();
		for (EntityRace playerRace : EntityRace.values()) {
			try {
				raceConfigs.put(playerRace, new BukkitRaceConfig(playerRace));
			} catch (Exception e) {
				Characters.getInstance().getLogger().severe("Encountered an issue while loading the configuration for the race '" + playerRace.name() + "'");
				MessageUtil.sendException(Characters.getInstance(), e);
			}
		}
	}

	@Override
	public String getDbPoolsId() {
		return dbpoolsId;
	}

	@Override
	public String getStatDescription(AbilityStat stat) {
		return statDescriptions.get(stat);
	}

	@Override
	public Location getCreateLocation() {
		return createLoc;
	}

	@Override
	public Location getStartLocation() {
		return startLoc;
	}
	
	@Override
	public List<String> getCreationStartText() {
		return creationStartText;
	}

	@Override
	public String getCreationStartNPC() {
		return creationStartNPC;
	}
	
	@Override
	public List<String> getCreationCreateText() {
		return creationCreateText;
	}

	@Override
	public long getMillisUntilOutOfPvpCombat() {
		return millisUntilOutOfPvp;
	}

	@Override
	public long getMillisUntilOutOfPveCombat() {
		return millisUntilOutOfPve;
	}

	@Override
	public int getMaxLevel() {
		return maxLevel;
	}

	@Override
	public long getExperienceBetweenLevels(int startingLevel, int endingLevel) {
		if (startingLevel == endingLevel) {
			return 0;
		} else if (startingLevel > endingLevel) {
			int num = startingLevel;
			startingLevel = endingLevel;
			endingLevel = num;
		}

		long total = 0;
		for (int i = startingLevel + 1; i <= Math.min(getMaxLevel(), endingLevel); i++) {
			total += xpToLevel.get(i);
		}

		return total;
	}

	// syntax: <world>,<x>,<y>,<z>
	private Location parseLocation(String str) {
		String[] parts = str.split(",");
		World world = Bukkit.getWorld(parts[0]);
		double x = Double.valueOf(parts[1]);
		double y = Double.valueOf(parts[2]);
		double z = Double.valueOf(parts[3]);

		return new Location(world, x, y, z);
	}

	@Override
	public ClassConfig getClassConfig(EntityClass playerClass) {
		return classConfigs.get(playerClass);
	}

	@Override
	public RaceConfig getRaceConfig(EntityRace playerRace) {
		return raceConfigs.get(playerRace);
	}
}
