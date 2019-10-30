package com.legendsofvaleros.modules.skills.core.admin;

import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.util.model.Model;
import org.bukkit.Material;

public abstract class AdminSkill extends Skill {
	public AdminSkill(String id, Material mat, Type type, double[] cooldown, Object[] description) throws IllegalArgumentException {
		super(id, type, null, null, null, cooldown, description);

		Model.put("skill-" + id, id, mat);
	}
}