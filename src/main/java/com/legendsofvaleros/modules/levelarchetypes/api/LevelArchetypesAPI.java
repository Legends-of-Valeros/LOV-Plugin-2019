package com.legendsofvaleros.modules.levelarchetypes.api;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/**
 * LevelArchetypes serves as a platform for dynamic base-stat configuration for both mobs and
 * players, as well as a centralized API for setting and getting the current levels of players and
 * mobs.
 * <p>
 * LevelArchetypes relies on other plugins to define the logic of what levels players and mobs are,
 * but provides a logical centralized API through which to access this information.
 */
public interface LevelArchetypesAPI {

    /**
     * Gets an archetype for a given id.
     * <p>
     * Archetypes contain broad configured information that can be used as baselines for groups of
     * mobs and/or players.
     * @param archetypeId The id of the archetype, as it appears in the configuration.
     * @return The archetype object for the given id, if one was found. Else <code>null</code>.
     * @see Archetype
     */
    Archetype getArchetype(String archetypeId);

    /**
     * Gets the current level of an entity.
     * @param entity The entity whose level to get.
     * @return The entity's level.
     */
    int getLevel(LivingEntity entity);

    /**
     * Registers a class to be the provider for levels of mobs of a certain type.
     * @param provider The class that will be able to definitively tell the level of every entity of
     *                 the given type.
     * @param types    The type(s) of entity to register the provider for.
     * @throws IllegalStateException On registering a type that has already been registered.
     */
    void registerLevelProvider(LevelProvider provider, EntityType... types) throws IllegalStateException;

}
