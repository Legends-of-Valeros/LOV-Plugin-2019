package com.legendsofvaleros.modules.gear.component.core;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.gear.component.GearComponent;
import com.legendsofvaleros.modules.gear.component.GearComponentOrder;
import com.legendsofvaleros.modules.gear.trigger.GearTrigger;
import com.legendsofvaleros.modules.gear.trigger.UseTrigger;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.item.GearType;
import com.legendsofvaleros.modules.gear.ItemUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

public class GearUsable {
	public static class Persist {
		public HashMap<RegeneratingStat, Integer> regenerating;
		public HashMap<AbilityStat, Integer> ability;
		public HashMap<Stat, Integer> stats;
	}

	public static class Component extends GearComponent<Persist> {
		private static final DecimalFormat DF = new DecimalFormat("#.00");
		
		@Override public GearComponentOrder getOrder() { return GearComponentOrder.EXTRA; }

		public int time = 0;
	
		public int cooldown = 0;
	
		public boolean consumed = false;
	
		public boolean decay = false;
	
		public HashMap<String, RangedValue> alter;
	
		public transient HashMap<RegeneratingStat, RangedValue> regenerating;
		public transient HashMap<AbilityStat, RangedValue> ability;
		public transient HashMap<Stat, RangedValue> stats;
		
		@Override
		public Persist onInit() {
			if(alter != null && alter.size() > 0) {
				if(regenerating == null && ability == null && stats == null) {
					base:
					for(Entry<String, RangedValue> entry : alter.entrySet()) {
						for(RegeneratingStat stat : RegeneratingStat.values())
							if(stat.name().equals(entry.getKey())) {
								if(regenerating == null)
									regenerating = new HashMap<>();
								regenerating.put(stat, entry.getValue());
								continue base;
							}

						for(AbilityStat stat : AbilityStat.values())
							if(stat.name().equals(entry.getKey())) {
								if(ability == null)
									ability = new HashMap<>();
								ability.put(stat, entry.getValue());
								continue base;
							}

						for(Stat stat : Stat.values())
							if(stat.name().equals(entry.getKey())) {
								if(stats == null)
									stats = new HashMap<>();
								stats.put(stat, entry.getValue());
								continue base;
							}
					}
				}
			}

			Persist persist = new Persist();
			
			if(regenerating != null) {
				persist.regenerating = new HashMap<>();
				for(Entry<RegeneratingStat, RangedValue> entry : regenerating.entrySet()) {
					int i = entry.getValue().intValue();
					if(i == 0) continue;
					persist.regenerating.put(entry.getKey(), i);
				}
			}
			
			if(ability != null) {
				persist.ability = new HashMap<>();
				for(Entry<AbilityStat, RangedValue> entry : ability.entrySet()) {
					int i = entry.getValue().intValue();
					if(i == 0) continue;
					persist.ability.put(entry.getKey(), i);
				}
			}
			
			if(stats != null) {
				persist.stats = new HashMap<>();
				for(Entry<Stat, RangedValue> entry : stats.entrySet()) {
					int i = entry.getValue().intValue();
					if(i == 0) continue;
					persist.stats.put(entry.getKey(), i);
				}
			}
			
			return persist;
		}

		@Override
		public double getValue(Gear.Instance item, Persist persist) {
			double power = 0;

			if(persist.regenerating != null) {
				if(persist.regenerating.containsKey(RegeneratingStat.HEALTH)) {
					power += persist.regenerating.get(RegeneratingStat.HEALTH) * .4D;
				}
			}

			return power;
		}

		@Override
		protected void onGenerateItem(Gear.Instance item, Persist persist, ItemBuilder builder) {
			if(alter == null || alter.size() == 0) return;
	
			if(persist.regenerating == null && persist.ability == null && persist.stats == null) {
				if(cooldown > 0)
					builder.addLore(ChatColor.GREEN + "Use: (" + DF.format(cooldown / 1000D) + " sec cooldown)");
				else
					builder.addLore(ChatColor.GREEN + "Use: Nothing");
				
			}else if(persist.regenerating != null && persist.ability == null && persist.stats == null && persist.regenerating.size() == 1) {
				Entry<RegeneratingStat, Integer> s = persist.regenerating.entrySet().iterator().next();
				builder.addLore(ChatColor.GREEN + "Use: " + (s.getValue() > 0 ? "Regenerates" : "Decreases") + " " + s.getKey().getUserFriendlyName() + " by " + Math.abs(s.getValue()) + ".");
				if(cooldown > 0)
					builder.addLore(ChatColor.GREEN + "(" + DF.format(cooldown / 1000D) + " sec cooldown)");
				
			}else if(persist.regenerating == null && persist.ability != null && persist.stats == null && persist.ability.size() == 1) {
				Entry<AbilityStat, Integer> s = persist.ability.entrySet().iterator().next();
				builder.addLore(ChatColor.GREEN + "Use: " + (s.getValue() > 0 ? "Increases" : "Decreases") + " " + s.getKey().getUserFriendlyName() + " by " + Math.abs(s.getValue()));
				StringBuilder timer = new StringBuilder(String.valueOf(ChatColor.GREEN));
				if(time > 0)
					timer.append(decay ? "decays over" : "for").append(" ").append(DF.format(time / 1000D)).append(" sec. ");
				if(cooldown > 0)
					timer.append("(").append(DF.format(cooldown / 1000D)).append(" sec cooldown)");
				builder.addLore(timer.toString().trim());
				
			}else if(persist.regenerating == null && persist.ability == null && persist.stats != null && persist.stats.size() == 1) {
				Entry<Stat, Integer> s = persist.stats.entrySet().iterator().next();
				builder.addLore(ChatColor.GREEN + "Use: " + (s.getValue() > 0 ? "Increases" : "Decreases") + " " + s.getKey().getUserFriendlyName() + " by " + Math.abs(s.getValue()));
				StringBuilder timer = new StringBuilder(String.valueOf(ChatColor.GREEN));
				if(time > 0)
					timer.append(decay ? "decays over" : "for").append(" ").append(DF.format(time / 1000D)).append(" sec. ");
				if(cooldown > 0)
					timer.append("(").append(DF.format(cooldown / 1000D)).append(" sec cooldown)");
				builder.addLore(timer.toString().trim());
				
			}else{
				builder.addLore(ChatColor.GREEN + "Use: " + (decay ? "Decays over" : "Lasts for") + " " + DF.format(time / 1000D) + " sec:");
				if(cooldown > 0)
					builder.addLore(ChatColor.GREEN + "(" + DF.format(cooldown / 1000D) + " sec cooldown)");
		
				if(persist.regenerating != null && persist.regenerating.size() > 0)
					for(Entry<RegeneratingStat, Integer> s : persist.regenerating.entrySet())
						builder.addLore(String.format(ChatColor.WHITE + "%s%s "+ ChatColor.WHITE + "%s", ItemUtil.getOperatorSign(s.getValue()), Math.abs(s.getValue()), s.getKey().getUserFriendlyName()));
		
				if(persist.ability != null && persist.ability.size() > 0)
					for(Entry<AbilityStat, Integer> s : persist.ability.entrySet())
						builder.addLore(String.format(ChatColor.WHITE + "%s%s "+ ChatColor.WHITE + "%s", ItemUtil.getOperatorSign(s.getValue()), Math.abs(s.getValue()), s.getKey().getUserFriendlyName()));
		
				if(persist.stats != null && persist.stats.size() > 0)
					for(Entry<Stat, Integer> s : persist.stats.entrySet())
						builder.addLore(String.format(ChatColor.WHITE + "%s%s "+ ChatColor.WHITE + "%s", ItemUtil.getOperatorSign(s.getValue()), Math.abs(s.getValue()), s.getKey().getUserFriendlyName()));
			}
		}
		
		@Override
		public Boolean test(Gear.Instance item, Persist persist, GearTrigger trigger) {
			if(!trigger.equals(UseTrigger.class)) return null;
			
			UseTrigger t = (UseTrigger)trigger;
			if(!t.getEntity().isPlayer()) return null;
			
			PlayerCharacter pc = Characters.getPlayerCharacter((Player)t.getEntity().getLivingEntity());
			
			if(cooldown > 0 && pc.getCooldowns().hasCooldown("usable-" + item.getID())) {
				MessageUtil.sendError(pc.getPlayer(), "You cannot use that for another " + (int)Math.ceil(pc.getCooldowns().getCooldown("usable-" + item.getID()).getRemainingDurationMillis() / 1000) + " seconds.");
				return false;
			}
	
			return true;
		}
		
		@Override
		public Persist fire(Gear.Instance item, Persist persist, GearTrigger trigger) {
			if(!trigger.equals(UseTrigger.class)) return null;
			
			UseTrigger t = (UseTrigger)trigger;
			if(!t.getEntity().isPlayer()) return null;
			
			PlayerCharacter pc = Characters.getPlayerCharacter((Player)t.getEntity().getLivingEntity());
			
			if(regenerating != null) {
				for(Entry<RegeneratingStat, Integer> s : persist.regenerating.entrySet())
					t.getEntity().getStats().editRegeneratingStat(s.getKey(), s.getValue());
			}
			
			ValueModifierBuilder v;
			
			if(ability != null) {
				for(Entry<AbilityStat, Integer> s : persist.ability.entrySet()) {
					v = pc.getAbilityStats().newAbilityStatModifierBuilder(s.getKey())
								.setDuration(time)
								.setRemovedOnDeath(true)
								.setValue(s.getValue())
								.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT);
					if(decay)
						v.setDecay(0, 1, time);
					v.build();
				}
			}
	
			if(stats != null) {
				for(Entry<Stat, Integer> s : persist.stats.entrySet()) {
					v = t.getEntity().getStats().newStatModifierBuilder(s.getKey())
								.setDuration(time)
								.setRemovedOnDeath(true)
								.setValue(s.getValue())
								.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT);
					if(decay)
						v.setDecay(0, 1, time);
					v.build();
				}
			}
			
			if(consumed) {
				item.amount--;

				trigger.requestStackRefresh();
			}
			
			if(cooldown > 0)
				pc.getCooldowns().offerCooldown("usable-" + item.getID(), Cooldowns.CooldownType.CHARACTER_PLAY_TIME, cooldown);
			
			if(item.getType() == GearType.POTION)
				pc.getPlayer().playSound(pc.getLocation(), "ui.potion.drink", 1F, 1F);

			return null;
		}
	}
}