package com.legendsofvaleros.modules.gear.component;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.trigger.CombatEntityTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.EquipTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.item.GearItem;
import org.bukkit.ChatColor;

public class SoulbindComponent extends GearComponent<Boolean> {
	@Override public GearComponentOrder getOrder() { return GearComponentOrder.EXTRA; }
	
	@Override public Boolean onInit() { return false; }

	@Override
	public double getValue(GearItem.Instance item, Boolean persist) {
		return 0;
	}

	@Override
	protected void onGenerateItem(GearItem.Instance item, Boolean persist, ItemBuilder builder) {
		if(persist)
			builder.addLore(ChatColor.GRAY + "Soulbound");
	}

	@Override
	public Boolean test(GearItem.Instance item, Boolean persist, GearTrigger trigger) {
		if(!trigger.equals(EquipTrigger.class)) return null;
		return persist || (trigger instanceof CombatEntityTrigger ? !((CombatEntityTrigger)trigger).getEntity().isPlayer() : true);
	}

	@Override
	public Boolean fire(GearItem.Instance item, Boolean persist, GearTrigger trigger) {
		if(persist || !trigger.equals(EquipTrigger.class)) return null;
		
		trigger.requestStackRefresh();
		
		return true;
	}
}