package com.legendsofvaleros.modules.classes.skills;

import java.util.List;
import java.util.Map.Entry;

/**
 * A set of skills available to a player-character.
 */
public interface SkillSet {

	// TODO respect silence

	void add(String skillId);
	Entry<Skill, Integer> remove(String skillId);
	
	List<String> getAll();
	List<Entry<Skill, Integer>> getSkills();

	/**
	 * Check if the user has levels in the skill.
	 */
	boolean has(String skillId);

	/**
	 * Always returns a value (unless skillId doesn't exist). If the user does not have
	 * the skill, it returns a level of zero.
	 */
	Entry<Skill, Integer> get(String skillId);

	/**
	 * Get the level of the skill.
	 */
	int getLevel(String skillId);

	// TODO passive skills?

}
