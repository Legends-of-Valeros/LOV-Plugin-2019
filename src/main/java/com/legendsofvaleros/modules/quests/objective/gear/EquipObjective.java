package com.legendsofvaleros.modules.quests.objective.gear;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.robert.item.NBTEditor;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.item.GearType;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class EquipObjective extends AbstractQuestObjective<Boolean> {
	private String id;
	
	private transient Gear item;

	@Override
	protected void onInit() {
		item = Gear.fromID(id);

		if(item == null)
			MessageUtil.sendException(GearController.getInstance(), "No item with that ID in quest. Offender: " + id + " in " + getQuest().getId());
	}

	@Override
	public Boolean onBegin(PlayerCharacter pc, Boolean progress) {
		return false;
	}

	@Override
	public boolean isCompleted(PlayerCharacter pc, Boolean progress) {
        return progress;
    }

	@Override
	public String getProgressText(PlayerCharacter pc, Boolean progress) {
		return "Equip " + (item == null ? "UNKNOWN" : item.getName());
	}

	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return "Equipped " + (item == null ? "UNKNOWN" : item.getName());
	}

	@Override
	public Class<? extends Event>[] getRequestedEvents() {
		return new Class[] { ArmorEquipEvent.class, ItemEquipEvent.class, ItemUnEquipEvent.class };
	}

	@Override
	public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
		if(id == null || item == null) return progress;

		if(event.getClass() == ArmorEquipEvent.class) {
			if(item.getType() != GearType.ARMOR) return progress;

			if(item.isSimilar(((ArmorEquipEvent)event).getNewArmorPiece()))
				return true;

		}else if(event.getClass() == ItemEquipEvent.class) {
			if(item.getType() == GearType.ARMOR) return progress;

			if(((ItemEquipEvent)event).getGear().getID().equals(id))
				return true;

		}else if(event.getClass() == ItemUnEquipEvent.class) {
			if(item.getType() == GearType.ARMOR) return progress;

			if(((ItemUnEquipEvent)event).getGear().getID().equals(id))
				return false;
		}

		return progress;
	}
}