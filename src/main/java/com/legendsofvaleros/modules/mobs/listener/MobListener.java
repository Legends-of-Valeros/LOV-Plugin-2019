package com.legendsofvaleros.modules.mobs.listener;

import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.core.CombatProfile;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityPreCreateEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;
import com.legendsofvaleros.modules.levelarchetypes.core.LevelArchetypes;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.npcs.NPCsController;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Map.Entry;

public class MobListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnloaded(ChunkUnloadEvent event) {
        for (org.bukkit.entity.Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (Mob.Instance.get(entity) == null) continue;

            CombatEngine.getInstance().killEntity((LivingEntity) entity);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Player)
            if (!NPCsController.getInstance().isNPC(event.getEntity()))
                return;

        switch (event.getSpawnReason()) {
            case CUSTOM:
            case EGG:
            case SPAWNER:
                break;
            default:
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCombatEntityPreCreate(CombatEntityPreCreateEvent event) {
        Mob.Instance instance = Mob.Instance.get(event.getLivingEntity());
        if (instance == null) return;

        Archetype archetype = LevelArchetypes.getInstance().getArchetype(instance.mob.getArchetype());
        if (archetype != null) {
            // Counting starts at 0. We must shift the level down by one for it to work properly.
            CombatProfile baseStats = archetype.getCombatProfile(Math.max(0, instance.level - 1));
            event.setCombatProfile(baseStats);
        }
    }

    @EventHandler
    public void onCreate(CombatEntityCreateEvent event) {
        Mob.Instance instance = Mob.Instance.get(event.getCombatEntity().getLivingEntity());

        if (instance == null) return;

        if (instance.mob.getStats() != null) {
            for (Entry<Object, Integer> stat : instance.mob.getStats().entrySet())
                event.getCombatEntity().getStats().newStatModifierBuilder((Stat) stat.getKey())
                        .setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT_IGNORES_MULTIPLIERS)
                        .setValue(stat.getValue())
                        .build();

            for (RegeneratingStat stat : RegeneratingStat.values()) {
                event.getCombatEntity().getStats().editRegeneratingStat(stat, event.getCombatEntity().getStats().getStat(stat.getMaxStat()));
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(CombatEngineDeathEvent event) {
        if (event.getDied().isPlayer()) return;

        Location loc = event.getDied().getLivingEntity().getLocation().clone().add(0, .5, 0);
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 8, .5D, 2D, .5D, 0D);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityCombust(EntityCombustEvent event) {
        // Return if interactive event
        if (event instanceof EntityCombustByBlockEvent
                || event instanceof EntityCombustByEntityEvent) {
            return;
        }
        if (event.getEntity() instanceof Zombie ||
                event.getEntity() instanceof Skeleton ||
                event.getEntity() instanceof PigZombie) {
            event.setCancelled(true);
        }
    }
}