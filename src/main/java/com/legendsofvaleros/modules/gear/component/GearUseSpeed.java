package com.legendsofvaleros.modules.gear.component;

import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.item.ItemBuilder.Attributes;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.trigger.DefendTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.EquipTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.UnEquipTrigger;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.util.DebugFlags;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			// builder.addAttributeMod(Attributes.ATTACK_SPEED, Attributes.Operation.ADD_NUMBER, persist.speed);
			
			builder.addLore(String.format(ChatColor.WHITE + "Attack Speed: %s/s", DF.format(persist.speed)));
		}

		@Override
		public GearUseSpeed.Persist fire(GearItem.Instance item, GearUseSpeed.Persist persist, GearTrigger trigger) {
			if(trigger.equals(EquipTrigger.class)) {
				((EquipTrigger)trigger).getEntity().getLivingEntity()
						.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(persist.speed);
			}else if(trigger.equals(UnEquipTrigger.class)) {
				((UnEquipTrigger)trigger).getEntity().getLivingEntity()
						.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4F); // 4 is the default player value
			}

			return null;
		}
	}
}