package com.legendsofvaleros.modules.gear.component;

import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.item.ItemBuilder.Attributes;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;

public class GearUseSpeed {
	public static class Persist {
		public double speed = 0;
	}
	
	public static class Component extends GearComponent<Persist> {
		private static final DecimalFormat DF = new DecimalFormat("#.00");
		
		@Override public GearComponentOrder getOrder() { return GearComponentOrder.DAMAGE; }
		
		public RangedValue speed;
		
		@Override
		public Persist onInit() {
			Persist persist = new Persist();
			persist.speed = (speed == null ? 1 : speed.doubleValue());
			return persist;
		}

		@Override
		public double getValue(GearItem.Instance item, Persist persist) {
			return 0;
		}

		@Override
		protected void onGenerateItem(GearItem.Instance item, Persist persist, ItemBuilder builder) {
			builder.addAttributeMod(Attributes.ATTACK_SPEED, Attributes.Operation.ADD_NUMBER, persist.speed - 4);
			
			builder.addLore(String.format(ChatColor.WHITE + "Speed: %s/s", DF.format(persist.speed)));
		}
	}
}