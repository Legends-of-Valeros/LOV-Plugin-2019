package com.legendsofvaleros.modules.skills.core.admin;

import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.Material;

public abstract class AdminSkill extends Skill {
	public AdminSkill(String id, Material mat, Type type, double[] cooldown, Object[] description) throws IllegalArgumentException {
		super(id, type, null, null, null, cooldown, description);

		Models.put("skill-" + id, id, mat);
	}
}