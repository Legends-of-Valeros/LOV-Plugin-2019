package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.modules.classes.stats.AbilityStat;
import com.legendsofvaleros.modules.classes.stats.AbilityStatApplicator;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.stats.StatModifierModel;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;

import java.util.Collection;
import java.util.List;

/**
 * Configuration options for a player-character class.
 */
public interface ClassConfig {

	List<String> getLongDescription();

	/**
	 * Gets the player-character class this configuration is for.
	 * 
	 * @return This player-character class this is a configuration for.
	 */
	EntityClass getPlayerClass();

	/**
	 * Gets the archetype that the class uses.
	 * <p>
	 * Archetypes define base info like base stats at different levels.
	 *
	 * @return The given class' archetype.
	 */
	Archetype getArchetype();

	/**
	 * Gets the base melee damage for the player-character class.
	 * 
	 * @return The base melee damage for the player-character class.
	 */
	double getBaseMeleeDamage();
	
	/**
	 * Gets the modifications to the base stats that each ability stat point gives per level.
	 * 
	 * @return The stat modifications for the ability stats in this player-character class.
	 */
	Collection<StatModifierModel> getModifiers(AbilityStat stat);

	/**
	 * Gets a new, per-player instance of an applicator for class stat values.
	 * 
	 * @param combatPlayer The combat data for the player that the applicator is for and will apply
	 *        the class stats' effects to.
	 * @return An object that will apply the effects of different levels of class stats for the given
	 *         class.
	 */
	AbilityStatApplicator getNewApplicator(CombatEntity combatPlayer);

}
