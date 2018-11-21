package com.legendsofvaleros.modules.gear.quest;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.robert.item.NBTEditor;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.gear.event.ItemUnEquipEvent;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.item.GearType;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class EquipObjective extends AbstractObjective<ObjectiveProgressBoolean> {
	private String id;
	
	private transient GearItem item;

	@Override
	protected void onInit() {
		item = GearItem.fromID(id);

		if(item == null)
			MessageUtil.sendException(Gear.getInstance(), "No item with that ID in quest. Offender: " + id + " in " + getQuest().getId(), false);
	}

	@Override
	public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
		return progress.value;
	}

	@Override
	public String getProgressText(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
		return "Equip " + item.getName();
	}

	@Override
	public String getCompletedText(PlayerCharacter pc) {
		return "Equip " + item.getName();
	}

	@Override
	public Class<? extends Event>[] getRequestedEvents() {
		return new Class[] { ArmorEquipEvent.class, ItemEquipEvent.class, ItemUnEquipEvent.class };
	}

	@Override
	public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressBoolean progress) {
		if(id == null || item == null) return;

		if(event.getClass() == ArmorEquipEvent.class) {
			if(item.getType() != GearType.ARMOR) return;

			ItemStack stack = ((ArmorEquipEvent)event).getNewArmorPiece();

			NBTEditor reader = new NBTEditor(stack);
			if(id.equals(reader.getString("lov.name")))
				progress.value = true;

		}else if(event.getClass() == ItemEquipEvent.class) {
			if(item.getType() == GearType.ARMOR) return;

			if(((ItemEquipEvent)event).getGear().gear.getID().equals(id))
				progress.value = true;

		}else if(event.getClass() == ItemUnEquipEvent.class) {
			if(item.getType() == GearType.ARMOR) return;

			if(((ItemUnEquipEvent)event).getGear().gear.getID().equals(id))
				progress.value = false;
		}
	}
}