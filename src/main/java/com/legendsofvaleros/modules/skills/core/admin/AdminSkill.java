package com.legendsofvaleros.modules.skills.core.admin;

import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Material;

public abstract class AdminSkill extends Skill {
	public AdminSkill(String id, Type type, double[] cooldown, Object[] description) throws IllegalArgumentException {
		super(id, type, null, null, null, cooldown, description);

		Model.put("skill-" + id, id, Material.NETHER_STAR);
	}
}