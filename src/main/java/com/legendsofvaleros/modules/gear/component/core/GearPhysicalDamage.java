package com.legendsofvaleros.modules.gear.component.core;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.gear.trigger.AttackTrigger;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.ChatColor;

public class GearPhysicalDamage {
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
		public double getValue(Gear.Instance item, Persist persist) {
			return persist.getAverageDamage() * 1.3D;
		}

		@Override
		protected void onGenerateItem(Gear.Instance item, Persist persist, ItemBuilder builder) {
			if(persist.max > 0) {
				double dps = (int)Math.round(ItemUtil.getAverageDPS(item) * 100D) / 100D;

				if(persist.min == persist.max)
					builder.addLore(String.format(ChatColor.WHITE + "%s Damage " + ChatColor.GRAY + "(%s dps)", persist.min, dps));
				else
					builder.addLore(String.format(ChatColor.WHITE + "%s - %s Damage " + ChatColor.GRAY + "(%s dps)", persist.min, persist.max, dps));
			}
		}

		@Override
		protected Persist fire(Gear.Instance item, Persist persist, GearTrigger trigger) {
			if(trigger.equals(AttackTrigger.class))
				((AttackTrigger)trigger).setDamage(ItemUtil.random_double(persist.min, persist.max));
			return null;
		}
	}
}