package com.legendsofvaleros.modules.characters.skill;

import java.util.List;
import java.util.Map.Entry;

/**
 * A set of skills available to a player-character.
 */
public interface SkillSet {

	// TODO respect silence

	void addCharacterSkill(String skillId);
	Entry<Skill, Integer> removeCharacterSkill(String skillId);
	
	List<String> getCharacterSkillIds();
	int getCharacterSkillLevel(String skillId);
	List<Entry<Skill, Integer>> getCharacterSkills();
	Entry<Skill, Integer> getCharacterSkill(String skillId);

	// TODO passive skills?

}
