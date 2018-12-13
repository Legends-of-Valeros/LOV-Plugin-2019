package com.legendsofvaleros.modules.levelarchetypes.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.levelarchetypes.api.Archetype;
import com.legendsofvaleros.modules.levelarchetypes.api.LevelArchetypesAPI;
import com.legendsofvaleros.modules.levelarchetypes.api.LevelProvider;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * LevelArchetypes serves as a platform for dynamic base-stat configuration for both mobs and
 * players, as well as a centralized API for getting the current levels of players and mobs.
 * <p>
 * LevelArchetypes relies on other plugins to define the logic of what levels players and mobs are,
 * but provides a logical centralized API through which to access this information.
 */
@DependsOn(CombatEngine.class)
public class LevelArchetypes extends ModuleListener implements LevelArchetypesAPI {

    public static final int MIN_LEVEL = 0;

    private static LevelArchetypes instance;
    public static LevelArchetypes getInstance() { return instance; }

    private Map<String, Archetype> archetypes;
    private Map<EntityType, LevelProvider> providers;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        archetypes = new HashMap<>();
        providers = new HashMap<>();

        FileConfiguration config = getConfig();
        ConfigurationSection archSec = config.getConfigurationSection("archetypes");
        if (archSec != null) {

            for (String key : archSec.getKeys(false)) {
                try {
                    archetypes.put(key, new LevelArchetype(key, archSec.getConfigurationSection(key)));

                } catch (Exception e) {
                    getLogger().severe("There was an issue while loading the archetype '" + key + "'");
                    MessageUtil.sendException(LevelArchetypes.getInstance(), e, false);
                }
            }
        }

        // gives clients time to register themselves as providers, then prints a warning if they do not
        Bukkit.getServer().getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> {
            List<String> types = new LinkedList<>();

            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && !providers.containsKey(type)) {
                    types.add(type.name());
                }
            }

            if (!types.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                sb.append("There is no level provider for living entities of the following types: [");
                int index = 0;
                for (String type : types) {
                    sb.append(type);
                    if (index++ < types.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]. This means that there is no way for plugins to tell what level these types of entities are.");

                LevelArchetypes.getInstance().getLogger().warning(sb.toString());

            }
        }, 80L);
    }

    @Override
    public Archetype getArchetype(String archetypeId) {
        if(archetypes == null) throw new RuntimeException("Archetypes is null. This shouldn't happen!");
        if(!archetypes.containsKey(archetypeId)) return null;
        return archetypes.get(archetypeId);
    }

    @Override
    public int getLevel(LivingEntity entity) {
        if (entity == null) {
            return MIN_LEVEL;
        }

        LevelProvider provider = providers.get(entity.getType());

        if (provider == null) {
            LevelArchetypes.getInstance().getLogger().warning(
                    "A client requested the level of a " + entity.getType().name()
                            + " but there is no provider registered for that type of entity.");
            return MIN_LEVEL;
        }

        try {
            return provider.getLevel(entity);
        } catch (Exception e) {
            LevelArchetypes.getInstance().getLogger().severe(
                    "Encountered an error while requesting a level for a " + entity.getType().name());
            MessageUtil.sendException(LevelArchetypes.getInstance(), entity instanceof Player ? (Player) entity : null, e, true);
            return MIN_LEVEL;
        }
    }

    @Override
    public void registerLevelProvider(LevelProvider provider, EntityType... types)
            throws IllegalStateException {
        if (provider == null || types == null || types.length < 1) {
            return;
        }

        for (EntityType type : types) {
            if (type != null) {
                if (providers.containsKey(type)) {
                    throw new IllegalStateException("Duplicate level providers for entities of type "
                            + type.name());
                }
                providers.put(type, provider);
            }
        }

    }
}
