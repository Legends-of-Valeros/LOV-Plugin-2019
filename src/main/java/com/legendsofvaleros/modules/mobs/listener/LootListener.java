package com.legendsofvaleros.modules.mobs.listener;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.DamageHistory;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.loot.LootController;
import com.legendsofvaleros.modules.loot.LootTable;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class LootListener implements Listener {
    private static final Random RAND = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(CombatEngineDeathEvent event) {
        if (event.getDied().isPlayer()) return;

        Mob.Instance entity = Mob.Instance.get(event.getDied().getLivingEntity());
        if (entity == null) return;
        if (entity.mob.getOptions().loot == null) {
            MessageUtil.sendError(Bukkit.getConsoleSender(), "No loot found for entity " + entity.mob.getName());
            return;
        }

        if (event.getKiller() == null || !event.getKiller().isPlayer() || !Characters.isPlayerCharacterLoaded((Player) event.getKiller().getLivingEntity())) {
            return;
        }

        //Get the person that did the most damage
        DamageHistory history = CombatEngine.getInstance().getDamageHistory(event.getDied().getLivingEntity());
        PlayerCharacter pc = Characters.getPlayerCharacter((Player) history.getHighestDamager());

        //fallback if nobody did damage before
        if (pc == null) {
            MessageUtil.sendDebug(Bukkit.getConsoleSender(), "Loot Listener - No damage history found for killer");
            pc = Characters.getPlayerCharacter((Player) event.getKiller());
        }

        Location dieLoc = event.getDied().getLivingEntity().getLocation();
        Map<String, AtomicInteger> connections = new HashMap<>();

        for (Mob.Options.LootData data : entity.mob.getOptions().loot) {
            AtomicInteger i;

            if (data.connect != null) {
                if (!connections.containsKey(data.connect)) {
                    connections.put(data.connect, new AtomicInteger());
                }
                i = connections.get(data.connect);
            } else {
                i = null;
            }

            LootTable table = LootController.getInstance().getTable(data.id);
            if (table == null) {
                MessageUtil.sendSevereException(LootController.getInstance(), event.getKiller().getLivingEntity(), "Mob has an unknown loot table. Offender: " + data.id + " on " + entity.mob.getId());
                return;
            }

            double chance = (data.chance == null ? table.chance : data.chance);
            if (table.chance == 0) {
                MessageUtil.sendSevereException(LootController.getInstance().moduleName, "Table has 0 drop chanche:" + table.id);
            }
            if (data.chance != null) {
                MessageUtil.sendDebug(Bukkit.getConsoleSender(), "Drop chanche of LootData" + data.id + ": " + data.chance);
            }

            for (int j = (i == null ? 0 : i.get()); j < data.amount; j++) {
                if (RAND.nextDouble() > chance) {
                    continue;
                }

                // If we have dropped all possible in the connection.
                if (i != null) {
                    if (data.amount - i.getAndIncrement() < 0) {
                        break;
                    }
                }

                LootTable.Item item = table.nextItem();
                if (item == null) {
                    continue;
                }
                ItemUtil.dropItem(dieLoc, item.getItem().newInstance(), pc);
            }
        }
    }
}
