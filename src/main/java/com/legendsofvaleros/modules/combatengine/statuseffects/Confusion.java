package com.legendsofvaleros.modules.combatengine.statuseffects;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder.ModifierType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gives an entity the confusion effect.
 */
public class Confusion {
	private static final double HIT_MULTIPLIER = 0.3;
	private static final PotionEffectType TYPE = PotionEffectType.CONFUSION;
	private static final PotionEffect EFFECT = new PotionEffect(TYPE, Integer.MAX_VALUE, 0);

	private static Map<UUID, ValueModifier> MODS = new HashMap<>();

	static {
		LogoutListener listener = new LogoutListener();
		JavaPlugin plugin = LegendsOfValeros.getInstance();
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
	}

	public static void apply(CombatEntity entity) {
		remove(entity);

		LivingEntity le = entity.getLivingEntity();
		if (le != null) {
			MODS.put(entity.getUniqueId(), entity.getStats().newStatModifierBuilder(Stat.HIT_CHANCE)
					.setModifierType(ModifierType.MULTIPLIER).setValue(HIT_MULTIPLIER).build());
			le.removePotionEffect(TYPE);
			le.addPotionEffect(EFFECT);
		}
	}

	public static void remove(CombatEntity entity) {
		ValueModifier mod = MODS.remove(entity.getUniqueId());
		if (mod != null) {
			mod.remove();
			LivingEntity le = entity.getLivingEntity();
			if (le != null) {
				le.removePotionEffect(TYPE);
			}
		}
	}

	/**
	 * Makes sure that the blindness potion effect does not persist if a player logs out while they
	 * are blinded.
	 */
	private static class LogoutListener implements Listener {

		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			event.getPlayer().removePotionEffect(TYPE);
		}
	}

}
