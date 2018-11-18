package com.legendsofvaleros.modules.characters.skill;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class PlayerSkillSet implements SkillSet {
	private HashMap<String, Entry<Skill, Integer>> characterSkills;
	
	public PlayerSkillSet(PlayerCharacter character, List<String> skillSet) {
		this.characterSkills = new HashMap<>();
		for(String skillD : skillSet) {
			String skillId = skillD;
			int level = 1;
			if(skillId.contains(":")) {
				String[] spl = skillId.split(":", 2);
				skillId = spl[0];
				level = Integer.parseInt(spl[1]);
			}
			
			Skill skill = Skill.getSkillById(skillId);
			if(skill != null) {
				if(level > skill.getMaxLevel())
					level = skill.getMaxLevel();
				this.characterSkills.put(skillId, new SimpleEntry<>(skill, level));
			}
		}
	}

	@Override
	public void addCharacterSkill(String skillId) {
		Skill skill = Skill.getSkillById(skillId);
		if(skill != null) {
			if(characterSkills.containsKey(skillId)) {
				Entry<Skill, Integer> sk = characterSkills.get(skillId);
				sk.setValue(sk.getValue() + 1);
			}else
				characterSkills.put(skillId, new SimpleEntry<>(skill, 1));
		}
	}

	@Override
	public Entry<Skill, Integer> removeCharacterSkill(String skillId) {
		Skill skill = Skill.getSkillById(skillId);
		if(skill != null)
			return characterSkills.remove(skillId);
		return null;
	}
	
	@Override
	public List<String> getCharacterSkillIds() {
		return new ArrayList<>(characterSkills.keySet());
	}

	@Override
	public int getCharacterSkillLevel(String skillId) {
		Entry<Skill, Integer> skill = characterSkills.get(skillId);
		if(skill == null)
			return 0;
		return skill.getValue();
	}
	
	@Override
	public List<Entry<Skill, Integer>> getCharacterSkills() {
		return new ArrayList<>(characterSkills.values());
	}
	
	@Override
	public Entry<Skill, Integer> getCharacterSkill(String skillId) {
		return characterSkills.get(skillId);
	}
}