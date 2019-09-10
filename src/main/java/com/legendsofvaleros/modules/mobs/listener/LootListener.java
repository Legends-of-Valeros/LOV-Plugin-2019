package com.legendsofvaleros.modules.mobs.listener;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.damage.DamageHistory;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.loot.api.ILootTable;
import com.legendsofvaleros.modules.mobs.api.IEntity;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;
import java.util.Random;

public class LootListener implements Listener {
    private static final Random RAND = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(CombatEngineDeathEvent event) {
        if (event.getDied().isPlayer()) return;

        Mob.Instance instance = Mob.Instance.get(event.getDied().getLivingEntity());
        if (instance == null) return;
        if (instance.entity.getLoot() == null) {
            MessageUtil.sendError(Bukkit.getConsoleSender(), "No loot found for entity " + instance.entity.getName());
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

        for (IEntity.Loot loot : instance.entity.getLoot()) {
            IEntity.Loot.Instance lootInstance = loot.newInstance();

            for(int t = 0; t < loot.tries; t++) {
                Optional<ILootTable> table = lootInstance.nextTable();
                if(table.isPresent()) {
                    ItemUtil.dropItem(dieLoc, table.get().nextItem().newInstance(), pc);
                }
            }
        }
    }
}