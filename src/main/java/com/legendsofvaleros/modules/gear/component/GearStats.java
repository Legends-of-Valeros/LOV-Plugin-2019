package com.legendsofvaleros.modules.gear.component;

import com.codingforcookies.robert.item.ItemBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.util.DebugFlags;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.field.RangedValue;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class GearStats {
	public static class Persist {
		public HashMap<AbilityStat, Integer> ability;
		public HashMap<Stat, Integer> stats;
	}
	
	public static class Component extends GearComponent<Persist> {
		@Override public GearComponentOrder getOrder() { return GearComponentOrder.STATS; }

		public HashMap<String, RangedValue> alter;

		public transient HashMap<AbilityStat, RangedValue> ability;
		public transient HashMap<Stat, RangedValue> stats;

		@Override public Persist onInit() {
			if(alter != null && ability == null && stats == null) {
				base:
				for(Entry<String, RangedValue> entry : alter.entrySet()) {
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
			
			Persist persist = new Persist();
			
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
		public double getValue(GearItem.Instance item, Persist persist) {
			double power = 0;

			if(persist.stats != null) {
				if(persist.stats.containsKey(Stat.ARMOR))
					power += persist.stats.get(Stat.ARMOR) * 1.7D;

				if(persist.stats.containsKey(Stat.MAX_HEALTH))
					power += persist.stats.get(Stat.MAX_HEALTH) * 2D;
			}

			return power;
		}
	
		@Override
		protected void onGenerateItem(GearItem.Instance item, Persist persist, ItemBuilder builder) {
			if(alter == null || alter.size() == 0) return;
	
			if(persist.stats != null && persist.stats.containsKey(Stat.ARMOR))
				builder.addLore(String.format(ChatColor.WHITE + "⛉ %s Armor", persist.stats.get(Stat.ARMOR)));
			
			if(persist.ability != null && persist.ability.size() > 0)
				for(Entry<AbilityStat, Integer> s : persist.ability.entrySet())
					builder.addLore(String.format(ChatColor.WHITE + "%s%s "+ ChatColor.WHITE + "%s", ItemUtil.getOperatorSign(s.getValue()), s.getValue(), s.getKey().getUserFriendlyName()));
	
			if(persist.stats != null && persist.stats.size() > 0)
				for(Entry<Stat, Integer> s : persist.stats.entrySet())
					if(s.getKey() != Stat.ARMOR)
						builder.addLore(String.format(ChatColor.WHITE + "%s%s "+ ChatColor.WHITE + "%s", ItemUtil.getOperatorSign(s.getValue()), s.getValue(), s.getKey().getUserFriendlyName()));
		}

		private transient Cache<CombatEntity, List<ValueModifier>> modifiers = CacheBuilder.newBuilder()
								.weakKeys()
								.build();

		@Override
		public Persist fire(GearItem.Instance item, Persist persist, GearTrigger trigger) {
			if(trigger.equals(EquipTrigger.class)) {
				EquipTrigger t = (EquipTrigger)trigger;
				
				PlayerCharacter pc = null;
				if(t.getEntity().isPlayer())
					pc = Characters.getPlayerCharacter((Player)t.getEntity().getLivingEntity());

				List<ValueModifier> mods = new ArrayList<>();

				if(ability != null && ability.size() > 0)
					for(Entry<AbilityStat, Integer> s : persist.ability.entrySet()) {
						if(pc != null) {
							if(DebugFlags.get(pc.getPlayer()).verbose)
								pc.getPlayer().sendMessage("Added " + s.getKey() + " " + s.getValue());

							mods.add(pc.getAbilityStats().newAbilityStatModifierBuilder(s.getKey())
								.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT)
								.setValue(s.getValue())
								.build());
						}
					}
		
				if(stats != null && stats.size() > 0)
					for(Entry<Stat, Integer> s : persist.stats.entrySet()) {
						if(pc != null && DebugFlags.get(pc.getPlayer()).verbose)
							pc.getPlayer().sendMessage("Added " + s.getKey() + " " + s.getValue());

						mods.add(t.getEntity().getStats().newStatModifierBuilder(s.getKey())
								.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT)
								.setValue(s.getValue())
								.build());
					}

				modifiers.put(t.getEntity(), mods);
			}else{
				CombatEntity removeFrom = null;

				if(trigger.equals(UnEquipTrigger.class)) {
					removeFrom = ((UnEquipTrigger)trigger).getEntity();

					MessageUtil.sendDebugVerbose(removeFrom.getLivingEntity(), "UNEQUIP: Removed all gear stats.");
				}else if(trigger.equals(DefendTrigger.class)) {
					if(item.hasComponent(GearDurability.Component.class)) {
						GearDurability.Persist durability = item.getPersist(GearDurability.Component.class);
						if(durability.max > 0) {
							if(durability.current == 0)
								removeFrom = ((DefendTrigger)trigger).getDefender();
							else{
								durability.current--;
								trigger.requestStackRefresh();
							}
						}
					}

					if(removeFrom != null) {
						MessageUtil.sendDebugVerbose(removeFrom.getLivingEntity(), "ATTACKED: Removed all gear stats.");
					}
				}

				if(removeFrom != null) {
					List<ValueModifier> mods = modifiers.getIfPresent(removeFrom);
					if(mods != null) {
						mods.forEach(ValueModifier::remove);
						modifiers.invalidate(removeFrom);
					}
				}
			}

			return null;
		}
	}
}