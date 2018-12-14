package com.legendsofvaleros.modules.gear.component;

import com.codingforcookies.robert.item.ItemBuilder;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.trigger.EquipTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.item.NoPersist;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RequireComponent extends GearComponent<NoPersist> {
	@Override public GearComponentOrder getOrder() { return GearComponentOrder.REQUIREMENTS; }
	@Override public NoPersist onInit() { return null; }

	public Integer level;
	@SerializedName("class")
	public EntityClass entityClass;
	@SerializedName("race")
	public EntityRace entityRace;

	@Override
	public double getValue(Gear.Instance item, NoPersist persist) {
		return 0;
	}

	@Override
	protected void onGenerateItem(Gear.Instance item, NoPersist persist, ItemBuilder builder) {
		StringBuilder sb = new StringBuilder(String.valueOf(ChatColor.YELLOW) + " ✓");
		
		if(level != null && level > 0) {
			sb.append(" Lvl ");
			sb.append(level);
		}
		
		if(entityRace != null) {
			sb.append(" ");
			sb.append(entityRace.getUserFriendlyName());
		}
		
		if(entityClass != null) {
			sb.append(" ");
			sb.append(entityClass.getUserFriendlyName());
		}
		
		builder.addLore(sb.toString());
	}

	@Override
	public Boolean test(Gear.Instance item, NoPersist persist, GearTrigger trigger) {
		if(!trigger.equals(EquipTrigger.class)) return null;
		
		EquipTrigger t = (EquipTrigger)trigger;
		if(!t.getEntity().isPlayer()) return true;
		
		PlayerCharacter pc = Characters.getPlayerCharacter((Player)t.getEntity().getLivingEntity());
		
		if(entityClass != null && entityClass != pc.getPlayerClass())
			return false;

		if(entityRace != null && entityRace != pc.getPlayerRace())
			return false;

		if(level != null && pc.getExperience().getLevel() < level)
			return false;
		
		return true;
	}
}