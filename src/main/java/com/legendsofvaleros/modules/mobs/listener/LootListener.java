package com.legendsofvaleros.modules.mobs.listener;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.loot.LootManager;
import com.legendsofvaleros.modules.loot.LootTable;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class LootListener implements Listener {
    private static final Random RAND = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(CombatEngineDeathEvent event) {
        if (event.getDied().isPlayer()) return;

        Mob.Instance entity = Mob.Instance.get(event.getDied().getLivingEntity());
        if (entity == null) return;
        if (entity.mob.getOptions().loot == null) return;

        if (event.getKiller() == null || !event.getKiller().isPlayer()) return;
        if (!Characters.isPlayerCharacterLoaded((Player) event.getKiller().getLivingEntity())) return;

        PlayerCharacter pc = Characters.getPlayerCharacter((Player) event.getKiller().getLivingEntity());

        Location dieLoc = event.getDied().getLivingEntity().getLocation();

        Map<String, AtomicInteger> connections = new HashMap<>();

        for (Mob.Options.LootData data : entity.mob.getOptions().loot) {
            AtomicInteger i;

            if (data.connect != null) {
                if (!connections.containsKey(data.connect))
                    connections.put(data.connect, new AtomicInteger());
                i = connections.get(data.connect);
            } else
                i = null;

            ListenableFuture<LootTable> future = LootManager.getInstance().getTable(data.id);
            future.addListener(() -> {
                try {
                    LootTable table = future.get();
                    if (table == null) {
                        MessageUtil.sendException(LegendsOfValeros.getInstance(), event.getKiller().getLivingEntity(), new Exception("Mob has an unknown loot table. Offender: " + data.id + " on " + entity.mob.getId()), true);
                        return;
                    }

                    double chance = (data.chance == null ? table.chance : data.chance);

                    for (int j = (i == null ? 0 : i.get()); j < data.amount; j++) {
                        if (RAND.nextDouble() > chance)
                            continue;

                        // If we have dropped all possible in the connection.
                        if (i != null)
                            if (data.amount - i.getAndIncrement() < 0) break;

                        LootTable.Item item = table.nextItem();
                        if (item == null) continue;

                        ListenableFuture<GearItem> futurestack = item.getItem();
                        futurestack.addListener(() -> {
                            try {
                                ItemUtil.dropItem(dieLoc, futurestack.get().newInstance(), pc);
                            } catch (InterruptedException | ExecutionException e) {
                                MessageUtil.sendException(LegendsOfValeros.getInstance(), event.getKiller() != null && event.getKiller().getLivingEntity() instanceof Player ? (Player) event.getKiller().getLivingEntity() : null, e, false);
                            }
                        }, Utilities.syncExecutor());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }, Utilities.asyncExecutor());
        }
    }
}
