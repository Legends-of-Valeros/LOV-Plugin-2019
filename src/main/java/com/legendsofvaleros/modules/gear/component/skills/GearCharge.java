package com.legendsofvaleros.modules.gear.component.skills;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.GearType;
import com.legendsofvaleros.modules.gear.trigger.CombineTrigger;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class GearCharge {
	public static final ChargeString[] STRINGS = {
			new ChargeString(1)
							.add("It shakes violently, overloaded with energy."),
		
			new ChargeString(.9)
							.add("It vibrates, filled with energy."),

			new ChargeString(.6)
							.add("It thrumms with mystical power."),
			
			new ChargeString(.4)
							.add("It pulsates softly."),

			new ChargeString(.2)
							.add("You can feel its power draining."),
			
			new ChargeString(0)
							.add("It feels lifeless."),
			
			new ChargeString()
							.add("You can sense no magic.")
	};
	
	public static class ChargeString {
		final Double min;
		final Multimap<GearType, String> strings = HashMultimap.create();

		ChargeString() { this.min = null; }
		ChargeString(double min) { this.min = min; }

		public ChargeString add(String text) { strings.put(null, text); return this; }

		public ChargeString add(GearType type, String text) { strings.put(type, text); return this; }
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

		@Override public double getValue(Gear.Instance item, Persist persist) {
			return persist.current;
		}

		@Override
		protected void onGenerateItem(Gear.Instance item, Persist persist, ItemBuilder builder) {
			if(persist.max <= 0)
				builder.addLore(ChatColor.AQUA + "❅ The extent of its power is unknowable.");
			else{
				// builder.addLore(ChatColor.AQUA + "❅ " + DF.format((double)persist.current / persist.max * 100) + "% Charge");
				double perc = (double)persist.current / persist.max;
				
				List<String> arr = new ArrayList<>();
				for(ChargeString dur : STRINGS) {
					if(dur.min != null && perc <= dur.min) continue;
					
					arr.addAll(dur.strings.get(null));
					arr.addAll(dur.strings.get(item.getType()));
					
					builder.addLore(ChatColor.AQUA + "❅ " + arr.get(item.getSeed() % arr.size()));
					break;
				}

				if(LegendsOfValeros.getMode().isVerbose())
					builder.addLore(ChatColor.AQUA + "❅ " + persist.current + "/" + persist.max);
			}
		}
		
		@Override
		public Boolean test(Gear.Instance item, Persist persist, GearTrigger trigger) {
			if(trigger.equals(CastTrigger.class)) {
				return persist.current > 0 || !((CastTrigger)trigger).skill.doesRequireFocus() || persist.max == 0;
			}else if(trigger.equals(CombineTrigger.class)) {
				if(persist.current > 0 || persist.max == 0) {
					CombineTrigger t = (CombineTrigger)trigger;
					if(t.getAgent().getType() == GearType.ARMOR
							|| t.getAgent().getType() == GearType.WEAPON)
						return false;
					Persist basePersist = t.getBase().getPersist(Component.class);
					if(basePersist == null)
						return false;
					return basePersist.current < basePersist.max;
				}
			}
			return null;
		}
	
		@Override
		public Persist fire(Gear.Instance item, Persist persist, GearTrigger trigger) {
			if(trigger.equals(CastTrigger.class)) {
				if(((CastTrigger)trigger).skill.doesRequireFocus() && persist.current > 0) {
					if(!LegendsOfValeros.getMode().isVerbose()) {
						double percCurr = (double) persist.current / persist.max;
						double percNew = (double) (persist.current - 1) / persist.max;
						for (ChargeString dur : STRINGS) {
							// If we reach a null minimum, then it won't ever be different.
							if (dur.min == null) break;

							// If the current minimum doesn't match, and the new one does, then refresh the stack.
							if (percCurr > dur.min && percNew <= dur.min) {
								trigger.requestStackRefresh();
								break;
							}
						}
					}else
						// Refresh the stack every time for testing purposes
						trigger.requestStackRefresh();
					
					persist.current--;
					
					return persist;
				}
			}else if(trigger.equals(CombineTrigger.class)) {
				CombineTrigger t = (CombineTrigger)trigger;
				Persist basePersist = t.getBase().getPersist(Component.class);
				if(basePersist.current + persist.current > basePersist.max || basePersist.max == 0) {
					basePersist.current = basePersist.max;
					
					if(basePersist.max > 0)
						persist.current -= basePersist.max - basePersist.current;
				}else{
					basePersist.current += persist.current;
					persist.current = 0;
				}

				trigger.requestStackRefresh();
				
				return persist;
			}
			
			return null;
		}
	}
}