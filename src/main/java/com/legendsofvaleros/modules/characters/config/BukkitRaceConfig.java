package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.entityclass.StatModifierModel;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of class configurations that is closely modeled to the default bukkit
 * configuration system.
 */
public class BukkitRaceConfig extends Configuration implements RaceConfig {

	private static final String SUBDIRECTORY = "races";

	private EntityRace playerRace;

	private List<String> descriptionLong;
	private List<String> descriptionClimate;

	private List<StatModifierModel> statModifiers;

	public BukkitRaceConfig(EntityRace playerRace) throws IllegalArgumentException,
	IllegalStateException {
		super(new YamlConfigAccessor(LegendsOfValeros.getInstance(), playerRace.name().toLowerCase()
				.replace("_", "-")
				+ ".yml", SUBDIRECTORY));
		this.playerRace = playerRace;

		load();
	}

	/**
	 * Reloads this configuration from disk.
	 */
	public void reload() {
		refresh();
		load();
	}

	private void load() {
		descriptionLong = getConfig().getStringList("long-description");
		descriptionClimate = getConfig().getStringList("climate-description");

		statModifiers = new ArrayList<>();
		ConfigurationSection perkSec = getConfig().getConfigurationSection("benefits");
		{
			List<StatModifierModel> mods = parseStatModifiers(perkSec.getStringList("stats"));
			statModifiers.addAll(mods);
		}
	}

	@Override
	public EntityRace getPlayerRace() {
		return playerRace;
	}

	@Override
	public List<String> getDescription() {
		return descriptionLong;
	}

	@Override
	public List<String> getClimateDescription() {
		return descriptionClimate;
	}

	@Override
	public Collection<StatModifierModel> getModifiers() {
		return statModifiers;
	}

	// Format: <STAT>,<MODIFIER TYPE>,<VALUE PER POINT>
	private List<StatModifierModel> parseStatModifiers(List<String> strs) {
		List<StatModifierModel> ret = new ArrayList<>(strs.size());

		for (String str : strs) {
			String[] parts = str.split(",");

			Stat modify = Stat.valueOf(parts[0]);

			ValueModifierBuilder.ModifierType modType = null;
			if ("ADD".equalsIgnoreCase(parts[1])) {
				modType = ValueModifierBuilder.ModifierType.FLAT_EDIT;
			} else if ("ADD_IGNORE_MULTIPLIERS".equalsIgnoreCase(parts[1])) {
				modType = ValueModifierBuilder.ModifierType.FLAT_EDIT_IGNORES_MULTIPLIERS;
			} else if ("MULTIPLY".equalsIgnoreCase(parts[1])) {
				modType = ValueModifierBuilder.ModifierType.MULTIPLIER;
			}

			double valuePerPoint = Double.valueOf(parts[2]);

			ret.add(new StatModifierModel(modify, modType, valuePerPoint));
		}

		return ret;
	}
}