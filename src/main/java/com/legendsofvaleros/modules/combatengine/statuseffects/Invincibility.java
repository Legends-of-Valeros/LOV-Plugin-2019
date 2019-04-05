package com.legendsofvaleros.modules.combatengine.statuseffects;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gives an entity the confusion effect.
 */
public class Invincibility {
	private static List<UUID> ENTITIES = new ArrayList<>();

	static {
		DamageListener listener = new DamageListener();
		CombatEngine.getInstance().registerEvents(listener);
	}

	public static void apply(CombatEntity entity) {
		remove(entity);

	    ENTITIES.add(entity.getUniqueId());
	}

	public static void remove(CombatEntity entity) {
		ENTITIES.remove(entity.getUniqueId());
	}

	/**
	 * Makes sure that the blindness potion effect does not persist if a player logs out while they
	 * are blinded.
	 */
	private static class DamageListener implements Listener {

		@EventHandler(priority = EventPriority.LOWEST)
		public void onDamaged(CombatEngineDamageEvent event) {
			if(ENTITIES.contains(event.getDamaged().getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}

}
