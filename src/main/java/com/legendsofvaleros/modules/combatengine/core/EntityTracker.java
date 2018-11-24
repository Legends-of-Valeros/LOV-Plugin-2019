package com.legendsofvaleros.modules.combatengine.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.UnsafePlayerInitializer;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityInvalidatedEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityPreCreateEvent;
import com.legendsofvaleros.modules.npcs.NPCs;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.UUID;

/**
 * Tracks entities and their combat data.
 * <p>
 * Uses various techniques to clean up entities that are no longer in use.
 */
public class EntityTracker implements UnsafePlayerInitializer {
    private CombatProfile defaultProfile;
    private Cache<UUID, CombinedCombatEntity> combatEntities;

    private boolean usePlayerInitializer;

    public EntityTracker(CombatProfile defaultProfile) {
        this.defaultProfile = defaultProfile;

        TrackingListener listener;
        CombatEngine.getInstance().registerEvents(listener = new TrackingListener());

        combatEntities =
                CacheBuilder.newBuilder().concurrencyLevel(1).weakValues().removalListener(listener)
                        .build();

        // TODO this would need to be configured if passive/aggressive initialization option is added
        // Run after all plugins are initialized
        CombatEngine.getInstance().getScheduler().executeInSpigotCircle(() -> {
            for (World world : Bukkit.getServer().getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity) {
                        getCombatEntity((LivingEntity) entity);
                    }
                }
            }
        });
    }

    public CombinedCombatEntity getCombatEntity(LivingEntity entity) {
        if (NPCs.isStaticNPC(entity)) return null;

        UUID uid = entity.getUniqueId();
        CombinedCombatEntity ce = combatEntities.getIfPresent(uid);

        // creates on access, unless using a manual player initializer and the entity is a player
        if (ce == null && (!usePlayerInitializer || entity.getType() != EntityType.PLAYER || NPCs.isNPC(entity))) {
            ce = create(entity);

        } else if (ce != null) {
            // refreshes the LivingEntity object in the CombatEntity object, in case a new one is being
            // used
            ce.refreshEntity(entity);
        }

        return ce;
    }

    // ---
    // start manual player initializer methods

    void usePlayerInitializer() {
        this.usePlayerInitializer = true;
    }

    @Override
    public void createCombatEntity(Player player) {
        if (player != null && combatEntities.getIfPresent(player.getUniqueId()) == null) {
            create(player);
        }
    }

    @Override
    public void invalidateCombatEntity(Player player) {
        if (player != null) {
            combatEntities.invalidate(player.getUniqueId());
        }
    }

    // end manual player initializer
    // ---

    private CombinedCombatEntity create(LivingEntity entity) {
        CombatEntityPreCreateEvent preEvent = new CombatEntityPreCreateEvent(entity, defaultProfile);
        Bukkit.getServer().getPluginManager().callEvent(preEvent);

        CombatProfile profile = preEvent.getCombatProfile();
        if (profile == null) {
            profile = defaultProfile;
        }

        CombinedCombatEntity ce = profile.createCombatEntity(entity);
        combatEntities.put(entity.getUniqueId(), ce);

        Bukkit.getServer().getPluginManager().callEvent(new CombatEntityCreateEvent(ce));

        // adds a player interface if the created combat object is for a player
        if (ce.isPlayer()) {
            ce.setPlayerCombatInterface(CombatEngine.getInstance().getPlayerInterface((Player) entity));
        }
        return ce;
    }

    /**
     * Performs cleanup for entities that no longer have any need for their stats.
     * <p>
     * Also initiates the construction of combat objects for certain entities.
     */
    public class TrackingListener implements Listener, RemovalListener<UUID, CombinedCombatEntity> {
        public TrackingListener() {
        }

        // notifies combat object and server of the object's invalidation
        @Override
        public void onRemoval(RemovalNotification<UUID, CombinedCombatEntity> notification) {
            CombinedCombatEntity ce = notification.getValue();
            if (ce != null) {
                ce.onInvalidated();

                // if the value is null, it would mean no one has any references to this object and it was
                // garbage collected, so the event is not really necessary
                Bukkit.getServer().getPluginManager()
                        .callEvent(new CombatEntityInvalidatedEvent(notification.getKey(), ce));
            }
        }

        // invalidates player's combat data on logout
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(final PlayerQuitEvent event) {
            Bukkit.getServer().getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> combatEntities.invalidate(event.getPlayer().getUniqueId()), 1L);
        }

        // notifies the combat object of death
        @EventHandler(priority = EventPriority.LOW)
        public void onEntityDeath(final EntityDeathEvent event) {
            final CombinedCombatEntity ce = combatEntities.getIfPresent(event.getEntity().getUniqueId());
            if (ce != null) {
                ce.onDeath();

                if (!ce.isPlayer()) {
                    Bukkit.getServer().getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> combatEntities.invalidate(event.getEntity().getUniqueId()), 1L);
                }
            }
        }

        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent event) {
            final CombinedCombatEntity ce = combatEntities.getIfPresent(event.getPlayer().getUniqueId());
            if (ce != null) {
                ce.onRespawn();
            }
        }

        // notifies the combat object of damage
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onValerosDamage(CombatEngineDamageEvent event) {
            CombinedCombatEntity ce = combatEntities.getIfPresent(event.getDamaged().getUniqueId());
            if (ce != null) {
                ce.onDamage();
            }
        }

        // forces creation of player combat data, if automatic creation is enabled
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            getCombatEntity(event.getPlayer());
        }

        // initializes combat data for spawning entities
        @EventHandler(priority = EventPriority.MONITOR)
        public void onEntitySpawn(CreatureSpawnEvent event) {
            if (!event.isCancelled())
                getCombatEntity(event.getEntity());
        }

        // initializes combat data for entities when they are loaded back into memory
        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            for (Entity entity : event.getChunk().getEntities()) {
                if (entity instanceof LivingEntity) {
                    CombinedCombatEntity cce = getCombatEntity((LivingEntity) entity);
                    // if the entity's CombatEntity object was still in the cache, its contained LivingEntity
                    // object needs to be set to the newly loaded version.
                    cce.refreshEntity((LivingEntity) entity);
                }
            }
        }

    }

}
