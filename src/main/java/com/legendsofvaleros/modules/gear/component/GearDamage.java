package com.legendsofvaleros.modules.gear.component;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.trigger.AttackTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.ChatColor;

public class GearDamage {
	public static class Persist {
		public int min = 0;
		public int max = 1;
		
		public double getAverageDamage() {
			return (max - min) / 2D + min;
		}
	}
	
	public static class Component extends GearComponent<Persist> {
		@Override public GearComponentOrder getOrder() { return GearComponentOrder.DAMAGE; }
		
		public RangedValue min, max;

		@Override
		public Persist onInit() {
			Persist persist = new Persist();
			persist.min = min.intValue();
			persist.max = max.intValue();
			if(persist.min > persist.max)
				persist.min = persist.max;
			return persist;
		}

		@Override
		public double getValue(GearItem.Instance item, Persist persist) {
			return persist.getAverageDamage() * 1.3D;
		}

		@Override
		protected void onGenerateItem(GearItem.Instance item, Persist persist, ItemBuilder builder) {
			if(persist.max > 0) {
				double dps = (int)Math.round(ItemUtil.getAverageDPS(item) * 100D) / 100D;

				if(persist.min == persist.max)
					builder.addLore(String.format(ChatColor.WHITE + "%s Damage " + ChatColor.GRAY + "(%s dps)", persist.min, dps));
				else
					builder.addLore(String.format(ChatColor.WHITE + "%s - %s Damage " + ChatColor.GRAY + "(%s dps)", persist.min, persist.max, dps));
			}
		}

		@Override
		protected Persist fire(GearItem.Instance item, Persist persist, GearTrigger trigger) {
			if(trigger.equals(AttackTrigger.class))
				((AttackTrigger)trigger).setDamage(ItemUtil.random_double(persist.min, persist.max));
			return null;
		}
	}
}