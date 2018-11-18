package com.legendsofvaleros.modules.characters.entityclass;

import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import org.bukkit.ChatColor;

/**
 * The types of classes/gameplay styles a player can play as.
 */
public enum EntityClass {

	WARRIOR(ChatColor.BLUE, "Warrior", RegeneratingStat.ENERGY),
	ROGUE(ChatColor.RED, "Rogue", RegeneratingStat.ENERGY),
	MAGE(ChatColor.DARK_PURPLE, "Mage", RegeneratingStat.MANA),
	PRIEST(ChatColor.GREEN, "Priest", RegeneratingStat.MANA);

	private ChatColor color;
	private String uiName;
	private RegeneratingStat skillCostType;

	EntityClass(ChatColor color, String uiName, RegeneratingStat skillUsePool) {
		this.color = color;
		this.uiName = uiName;
		this.skillCostType = skillUsePool;
	}

	public ChatColor getColor() {
		return color;
	}

	/**
	 * Gets a user-friendly name of this player class.
	 * 
	 * @return A user friendly name for this that can be used in user interfaces.
	 */
	public String getUserFriendlyName() {
		return uiName;
	}

	/**
	 * Gets the type of stat that this character uses primarily as the pool from which they have to
	 * spend in order to use skills/spells.
	 * 
	 * @return The primary stat which is used by this class to cast skills/spells.
	 */
	public RegeneratingStat getSkillCostType() {
		return skillCostType;
	}
	
	public static EntityClass getClassByName(String name) {
		for(EntityClass clazz : values())
			if(clazz.name().equals(name.toUpperCase()))
				return clazz;
		return null;
	}
}
