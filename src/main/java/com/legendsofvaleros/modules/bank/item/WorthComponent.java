package com.legendsofvaleros.modules.bank.item;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.trigger.CombineTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.PickupTrigger;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.item.GearType;
import com.legendsofvaleros.modules.skills.gear.CastTrigger;
import com.legendsofvaleros.modules.skills.gear.GearCharge;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.field.RangedValue;

public class WorthComponent extends GearComponent<Long> {
	@Override public GearComponentOrder getOrder() { return GearComponentOrder.LORE; }

	public RangedValue worth;

	@Override public Long onInit() { return worth == null ? 0 : worth.longValue(); }

	@Override
	public double getValue(Gear.Instance item, Long persist) {
		return persist;
	}

	@Override
	protected void onGenerateItem(Gear.Instance item, Long persist, ItemBuilder builder) {
		builder.addLore(Money.Format.format(persist));
	}

	@Override
	public Boolean test(Gear.Instance item, Long persist, GearTrigger trigger) {
		if(trigger.equals(PickupTrigger.class)) {
			return true;
		}else if(trigger.equals(CombineTrigger.class)) {
			CombineTrigger t = (CombineTrigger)trigger;
			Long amt = t.getBase().getPersist(WorthComponent.class);
			if(amt == null)
				return false;
			return true;
		}
		return null;
	}

	@Override
	protected Long fire(Gear.Instance item, Long persist, GearTrigger trigger) {
		if(trigger.equals(PickupTrigger.class)) {
			if (persist > 0) {
				PlayerCharacter pc = ((PickupTrigger) trigger).getPlayerCharacter();
				Money.add(pc, persist);

				MessageUtil.sendUpdate(pc.getPlayer(), "Picked up " + Money.Format.format(persist));
			}

			item.amount = 0;
		}else if(trigger.equals(CombineTrigger.class)) {
			CombineTrigger t = (CombineTrigger)trigger;
			Long amt = t.getBase().getPersist(WorthComponent.class);

			t.getBase().putPersist(WorthComponent.class, amt + persist);
			t.getAgent().amount = 0;

			trigger.requestStackRefresh();

			return 0L;
		}
		
		return null;
	}
}