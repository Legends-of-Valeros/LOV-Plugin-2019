package com.legendsofvaleros.modules.gear.component;

import com.codingforcookies.robert.item.ItemBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.gear.component.impl.GearComponent;
import com.legendsofvaleros.modules.gear.component.impl.GearComponentOrder;
import com.legendsofvaleros.modules.gear.component.trigger.AttackTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.component.trigger.PhysicalAttackTrigger;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.item.GearType;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class GearDurability {
	public static final DurabilityString[] STRINGS = {
			new DurabilityString(1)
							.add("There are no words to describe.").add("The most beautiful thing you've ever seen."),
		
			new DurabilityString(.99)
							.add("Brand spankin' new.")
							.add(GearType.WEAPON, "Mint condition.")
							.add(GearType.WEAPON, "It's perfectly sturdy."),
			
			new DurabilityString(.9)
							.add("It's in near perfect condition.").add("Essentially new."),

			new DurabilityString(.6)
							.add("Its been used a bit.").add("Starting to wear a bit.").add("Used slightly.").add("Hardly used.")
							.add(GearType.WEAPON, "It feels comfortable."),
			
			new DurabilityString(.4)
							.add("It's pretty worn.").add("It's a little worn out.").add("A bit tattered.").add("Used."),

			new DurabilityString(.2)
							.add("It has seen better days.").add("Needs some maintenance.")
							.add(GearType.ARMOR, "It's filled with holes."),
			
			new DurabilityString(0)
							.add("It's falling apart.").add("It could break at any moment."),
			
			new DurabilityString()
							.add("It's... broken.").add("Might as well throw this out.").add("A beautiful piece of garbage.")
	};
	public static class DurabilityString {
		final Double min;
		final Multimap<GearType, String> strings = HashMultimap.create();

		DurabilityString() { this.min = null; }
		DurabilityString(double min) { this.min = min; }

		public DurabilityString add(String text) { strings.put(null, text); return this; }

        public DurabilityString add(GearType type, String text) { strings.put(type, text); return this; }
    }
	
	public static class Persist {
		public int current;
		public int max;
	}
	
	public static class Component extends GearComponent<Persist> {
		@Override public GearComponentOrder getOrder() { return GearComponentOrder.DURABILITY; }
		
		public RangedValue current, max;
	
		@Override
		public Persist onInit() {
			Persist persist = new Persist();
			persist.current = current.intValue();
			persist.max = max.intValue();
			if(persist.current > persist.max)
				persist.current = persist.max;
			return persist;
		}

		@Override
		public double getValue(GearItem.Instance item, Persist persist) {
			return 0;
		}

		@Override
		protected void onGenerateItem(GearItem.Instance item, Persist persist, ItemBuilder builder) {
			if(persist.max <= 0)
				builder.addLore(ChatColor.AQUA + "❖ Unbreakable");
			else {
				// builder.addLore(String.format(ChatColor.WHITE + "%s/%s Durability", persist.current, persist.max));
				double perc = (double)persist.current / persist.max;
				
				List<String> arr = new ArrayList<>();
				for(DurabilityString dur : STRINGS) {
					if(dur.min != null && perc <= dur.min) continue;
					
					arr.addAll(dur.strings.get(null));
					arr.addAll(dur.strings.get(item.gear.getType()));
					
					builder.addLore(ChatColor.GRAY + "❖ " + arr.get(item.gear.getSeed() % arr.size()));
					break;
				}

				if(LegendsOfValeros.getMode().isVerbose())
					builder.addLore(ChatColor.GRAY + "❖ " + persist.current + "/" + persist.max);
			}
		}
	
		@Override
		public Persist fire(GearItem.Instance item, Persist persist, GearTrigger trigger) {
			if(trigger.equals(PhysicalAttackTrigger.class)) {
				if(persist.current > 0) {
					if(!LegendsOfValeros.getMode().isVerbose()) {
						double percCurr = (double) persist.current / persist.max;
						double percNew = (double) (persist.current - 1) / persist.max;
						for (DurabilityString dur : STRINGS) {
							// If we reach a null minimum, then it won't ever be different.
							if (dur.min == null) break;

							// If the current minimum matches, and the new one doesn't, then refresh the stack.
							if (percCurr > dur.min && percNew <= dur.min) {
								trigger.requestStackRefresh();
								break;
							}
						}
					}else
						// Refresh the stack every time for testing purposes
						trigger.requestStackRefresh();
					
					persist.current--;

					if(persist.current == 20)
						MessageUtil.sendError(((AttackTrigger)trigger).getAttacker().getLivingEntity(), "Your weapon is about to break! You'll do 25% your normal damage if you use it much longer!");
					
					return persist;
				}
				
				// ((DamageTrigger)trigger).event.setDamageMultiplier(.25D);
			}
			
			return null;
		}
	}
}