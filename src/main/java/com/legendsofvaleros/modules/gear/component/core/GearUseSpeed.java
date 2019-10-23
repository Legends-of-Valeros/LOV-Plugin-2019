package com.legendsofvaleros.modules.gear.component.core;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
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
		public double getValue(Gear.Instance item, Persist persist) {
			return 0;
		}

		@Override
		protected void onGenerateItem(Gear.Instance item, Persist persist, ItemBuilder builder) {
			// builder.addAttributeMod(Attributes.ATTACK_SPEED, Attributes.Operation.ADD_NUMBER, persist.speed);
			builder.addAttributeMod(ItemBuilder.Attributes.ATTACK_SPEED, ItemBuilder.Attributes.Operation.ADD_NUMBER, persist.speed - 4);

			builder.addLore(String.format(ChatColor.WHITE + "Attack Speed: %s/s", DF.format(persist.speed)));
		}

		@Override
		public GearUseSpeed.Persist fire(Gear.Instance item, GearUseSpeed.Persist persist, GearTrigger trigger) {
			/*if(trigger.equals(EquipTrigger.class)) {
				AttributeInstance attr = ((EquipTrigger)trigger).getEntity().getLivingEntity()
											.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
				attr.setBaseValue(persist.speed);

				Bukkit.broadcastMessage("s: " + attr.getBaseValue());
				Bukkit.broadcastMessage("v: " + attr.getValue());
				Bukkit.broadcastMessage("d: " + attr.getDefaultValue());
				for(AttributeModifier m : attr.getModifiers())
					Bukkit.broadcastMessage("m: " + m.getName() + " " + m.getAmount());
			}else if(trigger.equals(UnEquipTrigger.class)) {
				AttributeInstance attr = ((UnEquipTrigger)trigger).getEntity().getLivingEntity()
						.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
				attr.setBaseValue(attr.getDefaultValue());

				//((UnEquipTrigger)trigger).getEntity().getLivingEntity()
				//		.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4F); // 4 is the default player value
			}*/

			return null;
		}
	}
}