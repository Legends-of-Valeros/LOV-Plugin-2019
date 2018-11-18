package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStatApplicator;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.entityclass.StatModifierModel;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of class configurations that is closely modeled to the default bukkit
 * configuration system.
 */
public class BukkitClassConfig extends Configuration implements ClassConfig {

	private static final String SUBDIRECTORY = "classes";

	private List<String> descriptionLong;

	private EntityClass playerClass;
	private Archetype archetype;
	private double baseMeleeDamage;
	private Multimap<AbilityStat, StatModifierModel> abilityStatModifiers;

	public BukkitClassConfig(EntityClass configurationFor) throws IllegalArgumentException,
	IllegalStateException {
		super(new YamlConfigAccessor(Characters.getInstance(), configurationFor.name().toLowerCase()
				.replace("_", "-")
				+ ".yml", SUBDIRECTORY));
		this.playerClass = configurationFor;

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
		String archetypeName = getConfig().getString("archetype");
		archetype = LevelArchetypes.getInstance().getArchetype(archetypeName);
		if (archetype == null) {
			throw new IllegalArgumentException("The archetype '" + archetypeName + "' was not found");
		}

		baseMeleeDamage = getConfig().getDouble("base-melee-damage");

		abilityStatModifiers = HashMultimap.create();
		ConfigurationSection abilityStatSec = getConfig().getConfigurationSection("class-stats");
		for (String statName : abilityStatSec.getKeys(false)) {
			AbilityStat stat = AbilityStat.valueOf(statName.toUpperCase().replace(" ", "_"));

			List<String> effects = abilityStatSec.getStringList(statName);
			List<StatModifierModel> mods = parseStatModifiers(effects);
			for (StatModifierModel mod : mods) {
				abilityStatModifiers.put(stat, mod);
			}
		}

		descriptionLong = getConfig().getStringList("long-description");
	}

	@Override
	public List<String> getLongDescription() {
		return descriptionLong;
	}

	@Override
	public EntityClass getPlayerClass() {
		return playerClass;
	}

	@Override
	public Archetype getArchetype() {
		return archetype;
	}

	@Override
	public double getBaseMeleeDamage() {
		return baseMeleeDamage;
	}

	@Override
	public Collection<StatModifierModel> getModifiers(AbilityStat stat) {
		return abilityStatModifiers.get(stat);
	}

	@Override
	public AbilityStatApplicator getNewApplicator(CombatEntity combatPlayer) {
		return new AbilityStatApplicator(combatPlayer, abilityStatModifiers);
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