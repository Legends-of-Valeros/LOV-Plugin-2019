package com.codingforcookies.robert.window;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.slot.Slot;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

public class ExpandingGUI extends GUI {
	public ExpandingGUI(String title) {
		super(title);
	}

	public ExpandingGUI(String title, List<Slot> slots) {
		super(title);

		initSlots(slots);
	}

	public void initSlots(List<Slot> slots) {
		if(slots.size() <= 5)
			type(InventoryType.HOPPER);
		else
			type(InventoryType.DISPENSER);

		int i = 0;
		for(Slot slot : slots) {
			if(slots.size() == 1)
				slot(2, slot.stack, slot.action);
			else if(slots.size() == 2)
				slot(i == 0 ? 1 : 3, slot.stack, slot.action);
			else if(slots.size() == 3)
				slot(i * 2, slot.stack, slot.action);
			else if(slots.size() == 4)
				slot(i < 2 ? i : i + 1, slot.stack, slot.action);
			else if(slots.size() == 5)
				slot(i, slot.stack, slot.action);
			else if(slots.size() == 6)
				slot(i < 3 ? i : 3 + i, slot.stack, slot.action);
			else if(slots.size() == 7)
				slot(i < 3 ? i : i == 3 ? 4 : 2 + i, slot.stack, slot.action);
			else if(slots.size() == 8)
				slot(i < 3 ? i : i == 3 ? 3 : i == 4 ? 5 : 1 + i, slot.stack, slot.action);
			else if(slots.size() == 9)
				slot(i, slot.stack, slot.action);
			i++;
		}
	}
}