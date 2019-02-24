package com.legendsofvaleros.modules.characters.skill;

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

	boolean has(String skillId);
	Entry<Skill, Integer> get(String skillId);
	int getLevel(String skillId);

	// TODO passive skills?

}
