package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.config.CharactersConfig;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLevelUpEvent;
import com.legendsofvaleros.modules.characters.util.ProgressionUtils;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.core.CombatProfile;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

/**
 * Implements non-UI behavior on leveling up like increasing stats.
 */
public class LevelingListener implements Listener {

	private static final RegeneratingStat[] REGEN_STATS = RegeneratingStat.values();

	private CharactersConfig config;

	public LevelingListener(CharactersConfig config) {
		this.config = config;

		Bukkit.getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());
	}

	@EventHandler
	public void onPlayerCharacterLevelUp(PlayerCharacterLevelUpEvent event) {
		Player player = event.getPlayer();
		PlayerCharacter pc = event.getPlayerCharacter();
		int levelNow = event.getNewLevel();

		CombatEntity ce = CombatEngine.getEntity(player);
		EntityStats ceStats = ce.getStats();
		Archetype arch = config.getClassConfig(pc.getPlayerClass()).getArchetype();

		CombatProfile subtractThis = arch.getCombatProfile(levelNow - 1);
		CombatProfile fromThis = arch.getCombatProfile(levelNow);

		Map<Stat, Double> ceDifferences = ProgressionUtils.getProfileDifference(subtractThis, fromThis);
		for (Map.Entry<Stat, Double> ent : ceDifferences.entrySet()) {
			ceStats.newStatModifierBuilder(ent.getKey()).setValue(ent.getValue())
			.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT).build();
		}

		for (AbilityStat abilityStat : AbilityStat.values()) {
			double subtract = arch.getStatValue(abilityStat.name(), levelNow - 1);
			double from = arch.getStatValue(abilityStat.name(), levelNow);
			double difference = from - subtract;
			if (difference != 0.0) {
				pc.getAbilityStats().newAbilityStatModifierBuilder(abilityStat).setValue(difference)
				.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT).build();
			}
		}

		// heals each of the player's regenerating stats to full on leveling up
		for (RegeneratingStat stat : REGEN_STATS) {
			ceStats.setRegeneratingStat(stat, ceStats.getStat(stat.getMaxStat()));
		}
	}
}
