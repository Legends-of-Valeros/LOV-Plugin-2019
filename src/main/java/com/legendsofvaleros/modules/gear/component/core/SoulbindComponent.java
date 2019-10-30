package com.legendsofvaleros.modules.gear.component.core;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.trigger.CombatEntityTrigger;
import com.legendsofvaleros.modules.gear.trigger.EquipTrigger;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import org.bukkit.ChatColor;

public class SoulbindComponent extends GearComponent<Boolean> {
	@Override public GearComponentOrder getOrder() { return GearComponentOrder.EXTRA; }
	
	@Override public Boolean onInit() { return false; }

	@Override
	public double getValue(Gear.Instance item, Boolean persist) {
		return 0;
	}

	@Override
	protected void onGenerateItem(Gear.Instance item, Boolean persist, ItemBuilder builder) {
		if(persist)
			builder.addLore(ChatColor.GRAY + "Soulbound");
	}

	@Override
	public Boolean test(Gear.Instance item, Boolean persist, GearTrigger trigger) {
		if(!trigger.equals(EquipTrigger.class)) return null;
		return persist || (!(trigger instanceof CombatEntityTrigger) || !((CombatEntityTrigger) trigger).getEntity().isPlayer());
	}

	@Override
	public Boolean fire(Gear.Instance item, Boolean persist, GearTrigger trigger) {
		if(persist || !trigger.equals(EquipTrigger.class)) return null;
		
		trigger.requestStackRefresh();
		
		return true;
	}
}